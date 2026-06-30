# 장애 보고서 (Incident Report)

* **장애명**: 운영 환경(`rogic.io`) 프론트엔드 접속 불능 및 S3 `AccessDenied` 에러 발생
* **발생 일시**: 2026년 6월 30일 11:25 AM ~ (진행 중)
* **영향 범위**: 실 운영 사이트 (`https://rogic.io`, `https://www.rogic.io`) 전체 사용자 접속 불가
* **장애 등급**: Critical (서비스 중단)

---

## 1. 장애 요약 및 현상
- Terraform을 통해 ACM 인증서 검증 자동화 및 Route 53 A Alias 레코드 전환 작업을 완료한 직후, 운영 도메인(`rogic.io`) 접속 시 XML 형식의 `AccessDenied` 오류 메세지가 노출되며 화면이 렌더링되지 않음.
- 스테이징 환경(`stage.rogic.io`)은 동일한 작업을 거쳤으나 정상적으로 접속이 가능함.

---

## 2. 장애 원인 분석 (Root Cause Analysis)

### 2.1. 스테이징과 운영 환경 인프라 설정의 동시 수정 및 일괄 적용
- **문제점**: Staging 환경에서 변경 사항(Route 53 CNAME 검증 및 CloudFront Alias 지정)이 완벽히 동작하는지 최종 확인을 마치기 전에, Production 환경의 인프라 설정을 동시에 수정하여 Terraform 한 번의 명령(`apply`)으로 일괄 반영함.
- **결과**: 인프라 변경(DNS가 CloudFront를 거쳐 S3를 가리키도록 변경)이 Production에 즉시 적용되었으나, 해당 시점에 배포 흐름(CI/CD)의 준비 상태를 고려하지 못함.

### 2.2. 운영 S3 버킷의 정적 자원 부재 (Empty Bucket)
- **상황**: 스테이징 S3 버킷(`rogic-frontend-stage-ey12fmas`)에는 기존의 정적 빌드 파일이 올라가 있어 정상 서빙되었으나, 운영 S3 버킷(`rogic-frontend-prod-ey12fmas`)은 신규 인프라 배포 후 프론트엔드 빌드 결과물이 동기화(`aws s3 sync`)되지 않아 **완전히 비어 있는 상태**였음.
- **동작**: DNS 레코드가 CloudFront를 향하도록 변경되자마자 클라이언트 요청이 CloudFront -> S3 버킷의 `/index.html`을 조회하였고, S3 버킷 내 파일 부재 및 목록 조회 권한 제한 정책으로 인해 `AccessDenied` 응답이 클라이언트에 반환됨.

---

## 3. 타임라인 (Timeline)
* **11:18 AM**: ACM 자동 검증 및 CloudFront 설정 복원 완료.
* **11:25 AM**: Route 53 레코드를 CloudFront Alias로 전환하는 Terraform 변경 사항 적용 완료.
* **11:26 AM**: 운영 도메인(`rogic.io`) 접속 불능 및 `AccessDenied` 에러 발생 인지.
* **11:38 AM**: 운영 S3 버킷 조회를 통해 객체가 존재하지 않는 상태(Empty Bucket) 확인 및 원인 판명.

---

## 4. 복원 및 해결 조치 (Resolution)
1. **GitHub Actions 배포 파이프라인 트리거**:
   - 운영 환경 대상 프론트엔드 빌드 및 배포 워크플로우를 즉시 실행하여 운영 S3 버킷(`rogic-frontend-prod-ey12fmas`)으로 정적 리소스(HTML/JS/CSS)를 업로드(`aws s3 sync`)합니다.
2. **CloudFront 캐시 무효화 (Invalidation)**:
   - 파일 업로드 완료 후, CloudFront Edge에 기존 에러 응답 및 캐시를 갱신하기 위해 `/*` 경로에 대한 무효화 명령을 수행합니다.

---

## 5. 재발 방지 대책 (Preventative Measures)

### 5.1. 스테이징과 운영 인프라 변경 프로세스의 물리적 분리
- Terraform 코드상 환경별 변수 처리(Workspace 또는 Profile 분리)를 공고히 하여, 한 번의 `apply`로 Staging과 Production 인프라가 동시 반영되지 않도록 격리 조치합니다.
- 변경 사항은 항상 **Staging 배포 및 기능 검증 완료 -> 최소 24시간 모니터링 -> Production 승인 배포** 순서의 단계적 배포(Sequential/Stage-by-Stage Deployment) 방식으로 진행합니다.

### 5.2. DNS 레코드 전환 전 사전 정적 자원 시딩(Seeding)
- DNS 레코드를 신규 CDN/S3로 스위칭하기 전에, 대상 S3 버킷에 정상적인 웰컴 페이지나 플레이스홀더 빌드 파일을 먼저 올려둔 상태에서 레코드를 전환하여 무중단 상태를 보장하도록 검증 절차를 표준화합니다.
