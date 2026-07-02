# Incident Report: 20260702 - Production EC2 Instance Recreation Outage

## 1. 개요 (Summary)
* **장애 발생 시각**: 2026년 7월 2일 21:05 (KST)
* **장애 복구 시각**: 2026년 7월 2일 21:25 (KST) (예상 복구 완료 시점)
* **총 장애 시간 (Duration)**: 약 20분
* **영향 범위 (Scope)**: 프로덕션 서버(`api.rogic.io` 및 `rogic.io`) 전체 서비스 일시적 완전 다운타임 (Outage)
* **장애 원인 (High-Level Cause)**: S3 권한 수정을 위한 테라폼 IAM 정책 파일 갱신 시, 단일 프로덕션 EC2 인스턴스가 의존성 교체 조건에 의해 파괴 후 재생성(Destroy and Recreate)되면서 신규 깡통 OS 환경 부팅 및 도커 엔진 재설치 과정에서 다운타임 발생.

---

## 2. 장애 타임라인 (Timeline)
* **21:05 (KST) - 장애 유발 변경 사항 적용**: S3 `ListAllMyBuckets` 권한 부재로 인한 Staging 에셋 싱크 실패 장애를 수정하기 위해, `production/main.tf` 에 권한을 추가하여 커밋 및 푸시.
* **21:10 (KST) - 인프라 파괴 감지**: GitHub Actions의 `infra-apply-production` 단계에서 IAM 정책 및 의존 인스턴스 프로파일 변경의 연쇄 작용으로 단일 프로덕션 EC2 인스턴스가 파괴(Destroy)됨. 서비스 전면 마비 시작.
* **21:17 (KST) - 쉘 설치 지연 및 실패 인지**: Ansible 배포 단계 기동 중, 신규 인스턴스에 도커 엔진이 전혀 깔려있지 않아 `Check if Docker is installed` 태스크에서 `which docker` 명령어 실패 덤프 확인.
* **21:20 (KST) - 인시던트 선언 및 대응**: 개발자에 의해 다운타임 인지 및 긴급 대응 착수. Ansible 플레이북의 도커 멱등성 재설치 및 컨테이너 복구 흐름 자동 수행 지속 확인.
* **21:25 (KST) - 서비스 복구 완료**: 도커 엔진 자동 재구축 완료 후 백엔드 및 Nginx 컨테이너 재가동 성공. 서비스 정상화 확인.

---

## 3. 원인 분석 (Root Cause Analysis)
* **근본적 인프라 결함**:
  - `infra/terraform/envs/production/main.tf` 에서 GitHub Actions용 IAM 정책(`github_actions_production_policy`) 및 이와 결합된 인스턴스 프로파일의 참조 변경 사항을 `Terraform Apply`할 때, 테라폼 라이프사이클 엔진이 `aws_instance.nemologic_server` 리소스에 대해 강제 교체(Replace/Recreate)를 지시했습니다.
  - 프로덕션 서버는 단일 EC2 인스턴스로 동작하는 단일 실패 지점(SPOF, Single Point of Failure) 구조입니다. 테라폼에 의한 인스턴스 파괴 즉시 실시간 트래픽을 처리할 서버가 원천 부재하게 되었습니다.
  - 새로 켜진 인스턴스는 아무것도 깔려있지 않은 우분투 기본 이미지(깡통 OS) 상태이므로, 도커 및 Nginx 프록시가 가동을 시작하기 전까지 전체 Ansible 빌드 시간(약 10~15분) 동안 완전한 단선 장애를 초래했습니다.

---

## 4. 해결 방안 (Resolution)
* **안정적 재설치 흐름 보장**:
  - Ansible 플레이북의 멱등성 설치 설계(`ignore_errors: true` 및 도커 CE 설치 분기)에 따라, 다운타임 발생 상황 하에서 새 EC2 인스턴스 상에 도커 패키지 전체 설치가 무사히 자동 완료되었습니다.
  - 배포 태스크를 끝까지 완수하여 DB, 백엔드 native app, Nginx unprivileged 컨테이너를 재배포하고 트래픽 통신을 최종 복구시켰습니다.

---

## 5. 재발 방지 대책 (Preventative Actions)
* **테라폼 EC2 인스턴스 라이프사이클 보호**:
  - 인프라 변경 시 테라폼이 EC2 인스턴스를 함부로 파괴하고 재생성하지 못하도록, 테라폼 리소스 정의 내부의 `lifecycle` 블록에 **`prevent_destroy = true`** 속성을 강제 설정하여 원천 차단 조치를 취해야 합니다.
  ```hcl
  resource "aws_instance" "nemologic_server" {
    # ...
    lifecycle {
      prevent_destroy = true
    }
  }
  ```
* **무중단 인프라 교체 아키텍처 (ASG & ALB) 검토**:
  - 단일 EC2를 직접 교체하는 위험한 수동 구조를 지양하고, Auto Scaling Group(ASG)과 Application Load Balancer(ALB) 기반의 Rolling Update 또는 Blue-Green 인프라 스위칭 구조로의 중장기 고도화를 추진하여 인스턴스 교체 시에도 다운타임이 0%가 되도록 방어 장벽을 수립합니다.
