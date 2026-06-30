# 장애 보고서 (Incident Report)

* **장애명**: 운영 환경(`rogic.io`) 백엔드 API 통신 실패 및 SSL 핸드셰이크 오류 발생
* **발생 일시**: 2026년 6월 30일 13:53 PM ~ 14:12 PM (약 19분간 장애 지속)
* **영향 범위**: 실 운영 사이트 백엔드 API 호출 전체 불능 (로그인, 퍼즐 저장/조회 등 기능 중단)
* **장애 등급**: Major (일부 주요 기능 마비)

---

## 1. 장애 요약 및 현상
- 운영 환경의 프론트엔드 정적 파일 접속(S3)은 정상적으로 유지되었으나, 사용자가 퍼즐 플레이를 시도하거나 API 호출이 일어날 때 백엔드 통신 실패(502 Bad Gateway 및 SSL Handshake Failure) 오류가 노출되며 작동이 불가능한 현상 발생.

---

## 2. 장애 원인 분석 (Root Cause Analysis)

### 2.1. 인프라 배포(Terraform)와 서버 설정(Ansible)의 의존성 순서 불일치
- **구조적 한계**: 현재 CI/CD 파이프라인(`ci-cd.yml`) 상 `infra-apply` (Terraform) 단계가 `deploy-production` (Ansible) 단계보다 무조건 먼저 실행되도록 선후 구조가 짜여 있음.
- **결과**: CloudFront의 백엔드 원본 연결 정책을 `https-only`로 바꾸는 인프라 설정이 실제 EC2 Nginx 서버에 SSL 인증서(`api.rogic.io`)가 등록되기도 전에 선반영되어 SSL 핸드셰이크가 실패함.

### 2.2. CI/CD 동시성 설정에 의한 배포 생략 (`cancel-in-progress`)
- **상황**: 1단계(인증서 준비) 커밋이 푸시되어 파이프라인이 돌던 중, 2단계(CloudFront 전환) 커밋이 연속으로 푸시됨.
- **동작**: 워크플로우에 지정된 `concurrency.cancel-in-progress: true` 옵션으로 인해 진행 중이던 1단계 배포 작업(서버 실배포 및 인증서 발급)이 즉시 취소되고, 곧바로 2단계 테라폼 변경 사항만 적용되면서 인증서 공백 상태가 초래됨.

---

## 3. 타임라인 (Timeline)
* **13:53 PM**: `Phase 2B` (운영 CloudFront 원본 HTTPS 설정) 커밋 및 푸시 실행.
* **13:54 PM**: GitHub Actions 파이프라인 동시성 제어로 인해 이전 배포 단계가 자동 취소되고 테라폼 설정만 자동 반영됨.
* **14:08 PM**: 운영 서버 백엔드 API 접속 실패 감지 및 모니터링 확인.
* **14:10 PM**: 파이프라인 의존성 불일치 및 락 현상(DynamoDB State Lock) 파악 후 즉시 해제 조치 진행.
* **14:12 PM**: 취소되었던 운영 서버 배포(`deploy-production`) 단계를 수동 재트리거 및 정상 완료. SSL 인증서 발급 확인 후 백엔드 API 접속 정상 복구 완료.

---

## 4. 복원 및 해결 조치 (Resolution)
1. **상태 잠금 강제 해제**:
   - 로컬에서 `terraform force-unlock`을 실행하여 꼬여 있던 DynamoDB 상태 잠금을 해제함.
2. **운영 배포 수동 재트리거**:
   - 실패/취소 상태에 있던 `deploy-production` 단계를 GitHub Actions 상에서 수동 재실행하여 실 운영 EC2 Nginx 서버에 `api.rogic.io` 전용 SSL 인증서를 최종 배포 완료함.

---

## 5. 재발 방지 대책 (Preventative Measures)

### 5.1. 인프라 배포 시 동시성 취소 옵션 제거
- 중요 인프라 변경 및 서버 설정이 엮여 있는 핵심 파이프라인에 대해서는 `cancel-in-progress: false`로 설정하여 이전 배포 작업이 강제로 취소되지 않고 끝까지 순차 진행되도록 보장합니다.

### 5.2. 인프라와 설정 배포 흐름의 느슨한 결합 (Loose Coupling)
- 인프라 변경 사항(CloudFront Origin TLS 설정)은 즉시 자동 적용(Auto-Apply)하지 않고, 서버의 준비 상태를 검증한 후 사람이 수동 승인 게이트를 통해 릴리즈하도록 인프라 프로모션 방식을 개선합니다.
