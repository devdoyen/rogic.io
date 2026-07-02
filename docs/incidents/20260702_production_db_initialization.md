# Incident Report: 20260702 - Production Database Initialization and Data Loss

## 1. 개요 (Summary)
* **장애 발생 시각**: 2026년 7월 2일 21:05 (KST)
* **장애 인지 시각**: 2026년 7월 2일 22:45 (KST)
* **장애 복구 시각**: 2026년 7월 2일 23:10 (KST) (예상 조치 완료 시점)
* **총 장애 시간 (Duration)**: 약 2시간 5분 (실제 서비스 중단이 아닌 커스텀 데이터 유실 상태 방치 기간)
* **영향 범위 (Scope)**: 프로덕션 환경 데이터베이스(`nemologic-db`) 내의 사용자 생성 커스텀 퍼즐 데이터 완전 유실 및 기본 데모 데이터(8개) 재시딩 상태 노출.
* **장애 원인 (High-Level Cause)**: S3 권한 조정을 위한 IAM 정책 변경 시, 테라폼에 의해 프로덕션 EC2 인스턴스가 파괴 후 재생성되는 과정에서 로컬 Docker Named Volume에 담겨있던 DB 데이터가 영구 유실됨. 전역 S3 조회 권한(`s3:ListAllMyBuckets`) 부재로 백업 스크립트가 그동안 무음 실패(Access Denied)하여 S3 백업본이 존재하지 않아 데이터를 복원하지 못하고 데이터 초기화 상태가 노출됨.

---

## 2. 장애 타임라인 (Timeline)
* **21:05 (KST) - EC2 인스턴스 파괴 및 재생성**: `infra-apply-production` 단계에서 IAM 정책 및 의존 인스턴스 프로파일 변경의 연쇄 작용으로 단일 프로덕션 EC2 인스턴스(`nemologic_server`)가 파괴 및 재생성됨. 루트 EBS 볼륨과 함께 로컬 Docker Named Volume(`postgres_data`) 소멸.
* **21:25 (KST) - 새 인스턴스에서 DB 구동 및 자동 초기화**: 새 EC2 기동 후 Ansible 플레이북에 의해 백엔드 컨테이너가 최초 구동될 때 DB가 비어있는 상태이므로 `DataSeeder`가 기본 8개 데모 퍼즐을 재시딩함.
* **22:45 (KST) - 장애 인지**: 개발자가 프로덕션 Admin Console 화면에 진입하여 등록해 둔 커스텀 퍼즐이 사라지고 초기 데모 데이터만 노출되는 DB 초기화 장애 상황을 최초 발견 및 제보.
* **22:50 (KST) - 원인 분석**: S3 백업 버킷이 비어있음을 확인하고, 백업 스크립트(`backup_db.sh`) 내의 `aws s3 ls` 명령어가 EC2 IAM 역할의 `s3:ListAllMyBuckets` 권한 부재로 실패하고 있던 근본 원인을 파악.
* **23:10 (KST) - 복구 및 패치 적용**: EC2 인스턴스 보호 설정(`prevent_destroy = true`) 추가 및 Ansible 빌드 변수를 활용해 S3 백업 버킷명을 하드코딩 형식으로 안전하게 템플릿 처리하여 백업 무음 실패 결함을 해결.

---

## 3. 원인 분석 (Root Cause Analysis)
* **볼륨 소멸 메커니즘**:
  - 프로덕션 서버는 단일 EC2 인스턴스로 설계되어 있어, 테라폼의 IAM 설정 등 연쇄 변경 시 인스턴스가 재생성되면 기존 EBS 루트 볼륨이 함께 파괴됩니다.
  - PostgreSQL 데이터베이스 파일은 `/var/lib/postgresql/data`에 볼륨 매핑되어 있었으나, EBS 볼륨 자체가 소멸하면서 Named Volume 데이터도 함께 영구 삭제되었습니다.
* **백업 무음 실패 (Silent Failure)**:
  - `backup_db.sh` 스크립트는 S3 버킷명을 동적으로 획득하기 위해 `aws s3 ls` 명령어를 수행합니다.
  - 하지만 EC2 인스턴스용 IAM 정책(`nemologic-s3-backup-policy`)에는 보안 최소 권한 원칙(Least Privilege)에 따라 특정 백업 버킷에 대한 접근 권한만 있을 뿐, 전역 버킷 목록 조회 권한인 `s3:ListAllMyBuckets` 권한이 없었습니다.
  - 결과적으로 `aws s3 ls`는 `AccessDenied`를 반환하고 스크립트가 에러와 함께 종료되었습니다. cron 설정이 에러 스트림을 전량 누락(`> /dev/null 2>&1`)하도록 구성되어 있어 이 에러가 모니터링 시스템에 수집되지 못하고 무음 실패 상태로 누적되었습니다.

---

## 4. 해결 방안 (Resolution)

### 1) 테라폼 EC2 인스턴스 강제 파괴 방지 (`prevent_destroy = true`)
향후 테라폼 구성 변경으로 인해 인스턴스가 강제 파괴되는 위험을 원천 차단하기 위해 `lifecycle` 방어막을 staging/production 양쪽 EC2 리소스에 정의합니다.

```diff
# infra/terraform/envs/production/main.tf (및 staging/main.tf 동일)
 resource "aws_instance" "nemologic_server" {
   # ...
+  lifecycle {
+    prevent_destroy = true
+  }
 }
```

### 2) 백업 스크립트 S3 버킷 동적 조회 로직 제거 및 Ansible 변수 템플릿화
`aws s3 ls` 명령을 통한 동적 조회 방식 대신, GitHub Actions 러너가 테라폼 실행 후 S3 버킷명을 조회하여 Ansible에 변수로 제공하도록 개선합니다.

```diff
# infra/ansible/playbook.yml
     - name: Deploy DB backup script
       copy:
         content: |
           #!/bin/bash
-          # Find backup bucket name automatically from S3 list
-          BUCKET_NAME=$(aws s3 ls | grep nemologic-db-backup{{ '-stage' if env_name == 'stage' else '' }}{{ ' | grep -v stage' if env_name == 'prod' else '' }} | head -n1 | awk '{print $3}')
+          # Hardcoded bucket name configured via Ansible variable
+          BUCKET_NAME="{{ backup_bucket_name }}"
           if [ -z "$BUCKET_NAME" ]; then
               echo "Error: S3 Backup Bucket not found"
               exit 1
           fi
```

```diff
# .github/workflows/ci-cd.yml (staging/production 공통 수정)
       - name: Run Ansible Playbook (Staging)
+        env:
+          ...
+        run: |
+          cd infra/ansible
+          STAGE_BUCKET=$(aws s3 ls | grep rogic-frontend-stage | awk '{print $3}')
+          # Extract backup bucket name using runner's credential
+          BACKUP_BUCKET=$(aws s3 ls | grep nemologic-db-backup-stage | head -n1 | awk '{print $3}')
+          # ...
-          ansible-playbook -i hosts.ini playbook.yml --extra-vars "env=stage"
+          ansible-playbook -i hosts.ini playbook.yml --extra-vars "env=stage backup_bucket_name=$BACKUP_BUCKET"
```

---

## 5. 재발 방지 대책 (Preventative Actions)
1. **EC2 prevent_destroy 상시 적용**: 모든 환경(Staging, Production)의 핵심 영속성 인스턴스(DB 및 Application Server)에 `prevent_destroy = true`를 선언하여 테라폼 엔진에 의한 의도치 않은 파괴 행위를 사전에 차단합니다.
2. **크론탭 에러 모니터링 개선**: 백업 크론탭에서 표준 에러 스트림을 `/dev/null`로 무작정 버리지 않고, 에러 로그 파일(`/var/log/backup_db.err`)에 누적하도록 파이프라인 설정을 개선하여 무음 실패를 차단합니다.
3. **정기적 재해 복구(DR) 검증 테스트**: 백업본의 실효성을 담보하기 위해, 분기별로 S3에 올라간 데이터베이스 백업 파일(`.sql.gz`)을 다운로드하여 Staging 환경의 DB 서버에 복원하고 정상 기동 및 데이터를 검증하는 모의 복구(DR Drill) 절차를 의무화합니다.
4. **인프라 이중화 검토**: 영속적인 RDBMS 데이터를 EC2 로컬 도커 볼륨에 의존하는 단일 인스턴스 SPOF 구조에서, AWS RDS(PostgreSQL) 또는 EBS 독립형 볼륨 마운트 구조로의 전환을 장기 과제로 검토하여 컴퓨팅 인스턴스 교체 주기와 데이터 수명 주기를 완전히 분리합니다.
