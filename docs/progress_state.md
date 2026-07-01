# Progress State: 네모로직 코어 엔진 개발 진행

> [!NOTE]
> 본 프로젝트의 과거 진행 이력(Step 1 ~ Step 30)은 토큰 효율성 및 에이전트 과부하 방지를 위해 [progress_state_archive.md](./progress_state_archive.md)로 아카이빙(분리 이관)되었습니다.

## 0. 핵심 시스템 아키텍처 요약 (Core Architecture Summary)

* **프론트엔드 (Client)**: Vue 3 + TypeScript 기반 SPA. Canvas 2D API를 이용해 직교 격자 및 힌트를 렌더링하며 GPU 가속 CSS Transform `scale()` 줌 인/아웃 및 Confetti 이펙트 등을 내장.
* **백엔드 (API)**: Java 17 + Spring Boot 3.3.0 REST API. JPA(H2/PostgreSQL) 영속화 및 Flyway 마이그레이션 기반의 스키마 제어. DFS/백트래킹 기반의 노노그램 고속 분석기(`NonogramSolver`)로 문제 고유 해 무결성 검증.
* **배치 파이프라인 (AI Scheduler)**: 04:17 AM에 Gemini 3.1 Flash-Lite API를 비동기 호출해 규격별 퍼즐을 자동 생성 및 솔버 검증을 거쳐 예비 버퍼(크기별 최소 5개) 적재. 00:00 AM 자정에 선입선출(FIFO) 기반 무장애 릴리즈 수행.
* **배포 & 모니터링 (SRE)**: 512MB 저사양 EC2에서 작동하는 Active-Active Nginx 포트 스위칭 로드 밸런싱(Blue-Green). ACM SSL 자동 DNS 검증 및 CloudFront CDN 에지 캐싱. Grafana Mimir + CloudWatch logs + 이메일 긴급 경보(Synthetic Monitoring) 연동.

---

## 1. 현재 진행 상황 (Current Progress - Step 31 ~ 현재)

### 테라폼 Staging & Production 환경 및 네트워크 격리 (Step 31) - 완료
- **해결 내역**:
  - **코드 및 구조 분리**: root 경로의 단일 Terraform 파일군을 물리적으로 격리하여 `infra/terraform/envs/staging/` 및 `infra/terraform/envs/production/` 디렉토리 구조로 분할 완료함.
  - **네트워크 격리 (AWS VPC)**: Staging 인프라를 완전히 새로운 별도의 사설 가상 네트워크 대역인 Staging VPC(`10.1.0.0/16`, Subnet `10.1.1.0/24`)로 이전하여 Production VPC와의 내부망 경로를 완벽하게 물리 차단하고 단독 보안 그룹(Security Group) 규칙을 연동함.
  - **장부(State) 안전 마이그레이션**: 기존 라이브 운영 서버의 영속성 유지를 위해 기존 `terraform.tfstate` 장부를 승계하여 Staging 관련 리소스들만 `terraform state rm`으로 제거하고 Staging은 `staging/terraform.tfstate`로 신설함으로써 **서비스 중단 및 재생성 없는 무중단 인프라 구조 격리**를 달성함.
  - **CI/CD 파이프라인 고도화**: `.github/workflows/ci-cd.yml` 내의 테라폼 Plan & Apply 잡을 각각 Staging(`infra-plan-staging`, `infra-apply-staging`) 및 Production(`infra-plan-production`, `infra-apply-production`)으로 완전 쪼개어 배포 의존성 관계를 재구성함. Staging 인프라는 push 시 승인 없이 자동 배포(`auto-approve`)되고, Production 인프라만 GitHub Environment 수동 승인 게이트를 거쳐 배포되도록 안전 조치 및 DX 최적화를 동시에 구현 완료함.

### EC2 디스크 고갈 방지용 Docker 가비지 수집 자동화 (Step 32) - 완료
- **해결 내역**:
  - `playbook.yml` 최하단에 일 단위(매일 새벽 3시)로 가동되는 Docker Garbage Collection 크론탭 스케줄(`cron` 모듈)을 새롭게 배포 완료함.
  - `/usr/bin/docker system prune -af --volumes --filter 'until=72h'` 명령어를 스케줄링하여 72시간 동안 미사용된 구 버전 백엔드/Nginx 이미지, 만료된 볼륨, 잔여 캐시를 자동 소거하여 소용량 서버(`t3a.nano` RAM 500MB / EBS 8GB)의 디스크 100% 임계치 초과로 인한 다운 장애 리스크를 사전에 예방 완료함.

### SSL 자동 훅 검증 및 구버전 인증서 정리 (Step 33) - 완료
- **해결 내역**:
  - **갱신 훅 작동 검증**: Staging 및 Production EC2 호스트에서 `sudo certbot renew --dry-run` 테스트를 실행하여, 갱신 주기 도달 시 Nginx 정지/기동 훅(`renewal-hooks/pre`, `renewal-hooks/post`)이 정상 작동하며 SSL 검증 통과가 안전하게 완료됨을 최종 성공 검증함.
  - **노후화된 인증서 소거**: Production 서버에 불필요하게 남아있어 갱신 실패 에러(Exit 1)를 유발하던 구버전 Let's Encrypt 인증서(`rotagic.com`)를 `certbot delete --cert-name rotagic.com` 명령어로 깨끗하게 파기 완료함. 이로써 3개월 뒤 실서버 인증서 자동 갱신이 실패 없이 무중단으로 정상 갱신됨을 100% 보장함.

### Grafana 모니터링 대시보드 경량화 및 최적화 (Step 34) - 완료
- **해결 내역**:
  - **불필요 지표 소거**: 기존의 7,200줄에 달하던 무겁고 중구난방인 대시보드 스키마(`current_dashboard.json`)를 약 400줄 수준으로 94% 경량화함. 미사용한 JVM 스레드 상태, GC 소요 시간, Jetty 및 HikariCP 메트릭 등 60여 개의 불필요한 차트를 과감히 제거하여 대시보드 로딩 시간과 렌더링 성능을 최적화함.
  - **3대 핵심 모니터링 행(Row) 구성**:
    1. **SLA & API 성능**: API availability, 30-Day availability, HTTP Throughput(RPS), HTTP Latency(ms), HTTP status codes count.
    2. **OS 리소스**: CPU iowait & idle, RAM usage, Disk usage (Alloy 제거 및 node-exporter 단독 연동).
    3. **애플리케이션 로그**: AWS CloudWatch logs 뷰어 및 Logback ERROR/WARN 이벤트 발생 추이 차트.
  - **지표 단위 오류 수정**: 기존 CPU usage 패널의 데이터 단위가 메가바이트(`mbytes`)로 오표기되던 문제를 퍼센트(`percent`) 단위로 정상 수정함.
  - **HTTP Codes 및 Requests/second 'No Data' 해결**: Grafana Alloy 에이전트 제거 이후 Grafana Cloud에서 백엔드 Actuator 프로메테우스 endpoint를 직접 scraping(Agentless)함에 따라 수집 주기가 60초로 늘어남. 이로 인해 `rate(...[1m])`을 사용하는 쿼리들이 2개 이상의 데이터 포인트를 확보하지 못해 "No Data"가 출력되는 현상을 수정하기 위해, `[1m]` 구간을 Grafana dynamic 변수인 `[$__rate_interval]`로 변경하여 동적으로 최소 수집 주기 범위 이상을 확보하도록 개선함.
  - **Nginx 헬스체크 및 스크래핑 로그 차단**: 주기적인 헬스체크 및 프로메테우스 스크래핑 호출로 인해 발생하던 불필요한 Nginx Access Log 스팸이 CloudWatch 로그 그룹을 독점하고 애플리케이션 로그 분석을 저해하는 현상을 해결하기 위해, `nginx.prod.conf` 및 `nginx.stage.conf`에서 `/actuator/health`, `/actuator/prometheus`, `/node-metrics` 경로에 대해 `access_log off;` 설정을 적용 완료함.
  - **JVM 및 DB 커넥션 풀(HikariCP) 모니터링 패널 추가**: 메모리 누수(OOM) 및 DB 커넥션 병목 현상의 선제적인 정밀 분석과 모니터링을 위해, 대시보드 내에 `JVM Heap Memory Usage` (Used vs Max, MB 단위) 패널 및 `HikariCP Connection Pool` (Active, Pending, Max) 패널을 포함하는 `JVM & Database Pool Metrics` 행(Row) 설정을 신규 추가 완료함.
  - **Staging 환경 자동화 Playwright 브라우저 E2E 테스트 도입**: Staging 배포 후 수동 테스트 과정을 실제 사용자와 동일하게 웹 브라우저 수준에서 자동 검증하기 위해 **Playwright** 테스트 프레임워크를 도입함. 홈 화면 노출 여부, 게임 플레이 탭 진입 및 Nonogram Canvas 렌더링 검증, My Page 탭에서의 익명 사용자 자동 가입 및 레벨/XP 노출 정합성을 실제 헤드리스 브라우저 환경에서 검증하는 E2E 시나리오(`frontend/e2e/staging.spec.ts`)를 작성하고 CI/CD 워크플로우에 연동 완료함. 이로써 완벽히 검증된 배포본만 운영 승인 대기 상태로 이행하도록 보장함.

### Grafana 대시보드 내 SLA 및 신뢰도 패널 복구 (Step 35) - 완료
- **해결 내역**:
  - 간소화 과정에서 유실되었던 핵심 SRE 지표인 **Incident Count (24h/7d/30d)**, **Service Availability (24h/7d/30d)**, **MTTR (24h/7d/30d)**, **MTBF (24h/7d/30d)** 등 총 11개 패널을 `current_dashboard.json`에 완전 복구 완료함.
  - **그리드 레이아웃 최적화**: 
    - 첫 번째 행(`Nemologic Service SLA Metrics`)을 24가로폭 그리드에 최적화하여 3개 열 배치(각 폭 8) 구조 of 카드 행렬 레이아웃을 구성함.
    - `y=0`: API Health Status (폭 12), HTTP Codes (폭 12)
    - `y=4`: 24h / 7d / 30d Service Availability (각 폭 8)
    - `y=8`: 24h / 7d / 30d Incident Count (각 폭 8)
    - `y=12`: 24h / 7d / 30d MTTR (각 폭 8)
    - `y=16`: 24h / 7d / 30d MTBF (각 폭 8)
    - `y=20`: Requests/second (폭 12), Requests duration (폭 12)
  - **정합성 및 검증**: python의 json 라이브러리를 통해 대시보드 json 구문 오류가 없음을 검증하고, 전체 백엔드(32개) 및 프론트엔드(68개) 단위 테스트를 실행하여 100% 정상 통과함을 확인함.

### 30x30 대형 퍼즐 생성 예외 해결 및 프롬프트 최적화 (Step 36) - 완료
- **해결 내역**:
  * **현상**: 매일 04:17 AM에 자동 가동되는 AI 데일리 퍼즐 생성기(`DailyPuzzleScheduler`)가 30x30 크기의 퍼즐을 생성하는 도중 JSON 파싱 오류(`Unrecognized token 'Array': was expecting...`)를 일으키며 작업이 중단되고 CloudWatch 경보가 감출됨.
  * **원인 분석**: 30x30 격자는 총 900개의 정수를 전달해야 하므로 텍스트 소비가 급증함. 이에 따라 Gemini-3.1-flash-lite 모델이 출력 길이 단축을 위해 리터럴 배열 대신 JavaScript 단축 표현식(`Array(30)...`)을 반환하여 백엔드 Jackson 파서에서 예외가 유발됨.
  * **해결 조치**: 
    1. `GeminiAiClient.java` 내 AI 프롬프트를 보강하여 어떠한 형태의 프로메트릭 코드, 축약 표현식, 루프 함수도 사용 금지하고, 모든 칸을 명시적인 `0`과 `1`로만 구성된 `literal 2D JSON array` 형식으로만 출력하도록 강제함.
    2. API 토큰 한계와 응답 단절을 예방하기 위해, 25x25 이상 크기의 퍼즐 생성 시 AI 후보(Candidates) 개수를 기존 5개에서 2개로 최적화 및 축소함.
    3. 장애 발생 즉시 원인 규명 및 방지 대책을 작성한 장애 포스트모템 보고서([20260701_daily_puzzle_generation_failure.md](file:///c:/Users/82107/dev/project/nemologic/docs/incidents/20260701_daily_puzzle_generation_failure.md))를 신설 및 보관 완료함.
    4. 전체 백엔드 단위/통합 테스트를 재실행하여 100% 정상 통과함을 보장함.

### README.md 내 Troubleshooting 대분류별 소분류 신설 및 실제 장애 사례 기록 (Step 37) - 완료
- **해결 내역**:
  - 포트폴리오 가독성 향상과 실무 트러블슈팅 경험의 명확한 부각을 위해 `README.md`의 대분류(1. Infra, 2. CI/CD, 3. AI Engineering) 하단에 각각 전용 `Troubleshooting` 소분류(`1.5`, `2.4`, `3.3`)를 신설함.
  - **1.5.1 (Infra SRE)**: t3a.nano(512MB RAM)의 메모리 한계(OOM) 문제 해결을 위해 에이전트리스 풀(Agentless Pull) 아키텍처 전환 및 GraalVM Native Image 고도화 적용 사례를 기록함. 특히, AI의 단순 스케일업 및 ALB/RDS 도입 권장에 의존하지 않고 주도적으로 아키텍처 트레이드오프를 도출하여 해결한 개발자 회고(Retrospective)를 작성함.
  - **2.4.1 (CI/CD)**: Staging-Production 파이프라인 강결합으로 발생했던 정적 자산 부재 접속 장애 및 `cancel-in-progress` 설정에 따른 SSL 핸드셰이크 장애 극복 사례(Terraform 인프라 환경 물리 격리, `cancel-in-progress: false` 적용, Nginx/CloudFront 의존성 제어를 위한 수동 승인 게이트 결합)를 상세 기재함.
  - **3.3.1 (AI Pipeline)**: 경량 Gemini 모델이 30x30 대형 그리드 생성 시 데이터 생략을 위해 JS 배열 코드를 반환하던 파싱 에러 복구 사례(엄격한 리터럴 JSON 프롬프트 제약 및 대형 연산 후보군 축소를 통한 토큰 조율)를 3.3 섹션으로 이관 및 정리함.
  - **가이드라인 규칙 신설 및 문서 포맷 리팩토링**: 마크다운 문서 내 트러블슈팅 및 상세 소제목 기술 시 개행 표준화 및 절대 경로(`file:///`) 삽입 금지 조치를 포함한 신규 거버넌스 규칙 [documentation-guidelines.md](../.agents/rules/documentation-guidelines.md)를 제정함. 수치 대조 표(Table) 우선 적용 규칙을 추가하고 README.md에 비용 비교 테이블을 구현함. 또한, 개발 작업 완료 후의 커밋 메시지 컨벤션(Conventional Commits 규칙) 확립 및 로컬 커밋 자동 수행과 원격 푸시(Push) 권한 위임 거버넌스를 제정하는 신규 규칙 [git-and-commit-guidelines.md](../.agents/rules/git-and-commit-guidelines.md)를 추가 수립함. 아울러 에이전트 규칙 변경 사항을 대외적으로 공개하기 위해 README.md에 `3.2.3. AI Agentic Development & Governance` 섹션 내 찾아보기 규칙 테이블을 통합하고 4.3 섹션을 최종 걷어냄. 비용 최적화와 기술적 한계 분석의 층위 독립을 위해, 기존 `1.2` 하위로 구성되어 있던 기술적 타협 및 완화 방안을 **독립 섹션 `1.3. Technical Trade-offs & Mitigations`로 분리**하고, 이에 맞춰 하위 후속 장표 인덱스(`1.4` 네트워크, `1.5` 관제, `1.6` 트러블슈팅) 및 TOC 내부 앵커 링크들을 전면 재구성하여 가독성과 서술 깊이를 향상함.
  - **관제 대시보드 전역 연동형 최적화 및 CI/CD 병목 해결**: 중복 배치가 심해 시인성이 낮던 기존의 24h/7d/30d 고정 배치 SLA 지표 카드들(12개)을 전면 걷어내고, Grafana 우측 상단 Time Range Picker와 완전히 상호작용하는 **전역 연동형 대시보드 구조(KPI 카드 4개)**로 `current_dashboard.json` 설정을 경량 리팩토링함. 이를 위해 각 PromQL 수식 내 하드코딩되어 있던 범위 벡터 기호(`[24h]`, `[7d]`, `[30d]`)들을 Grafana 동적 매크로 변수인 `[$__range]` 지시자로 전부 치환하고, 이에 맞춰 README.md 내의 1.5.4 쿼리 부록 명세를 함께 갱신함. 아울러 대시보드 등 인프라 설정 변경(Terraform/Monitoring) 시 배포가 생략되는 CI/CD 병목을 조치하기 위해, `ci-cd.yml`의 `changes` 감지 잡에 `terraform` 필터(감지 대상: `infra/terraform/**`, `infra/monitoring/**`)를 추가하고, `deploy-staging` 잡의 `if` 활성화 조건에 오알(OR) 조건으로 연동하여 인프라 형상 단독 변경 시에도 프로덕션 배포 파이프라인(Terraform Production Apply)이 스킵 없이 정상 구동되도록 해결함.
  - **목차 가독성 최적화**: 깃허브 마크다운 렌더러에서의 인라인 CSS 필터링(무시) 현상을 대응하기 위해, 들여쓰기와 인라인 스타일링을 전면 걷어내고 정갈한 좌측 정렬 기반의 줄바꿈 리스트 구조로 리팩토링하여 뷰어 정합성과 가독성을 일괄 조치함.
  - **C4 Model 기반 인라인 Mermaid 다이어그램 고도화 및 레거시 폐기**: 인프라 아키텍처 다이어그램 구성을 C4 Model 사상에 기반한 순수 텍스트 인라인 Mermaid 문법으로 전환 완료함. README.md 내에 기존의 난해하게 얽혀 있던 Inframap 리소스 의존성 차트(1.1.3) 및 png 이미지 파일 링크 구조를 완벽히 걷어내고, Level 1 System Context (`C4Context`), Level 2 Network Context (`C4Container`), Level 3 Telemetry Context (`C4Container`) 다이어그램을 마크다운 내에 인라인 텍스트 코드로 완벽하게 내장하여 원격 뷰어 호환성 및 이식성을 극대화함. 이에 따라 더이상 불필요해진 `docs/diagram.py` 파이썬 빌드 스크립트 및 관련 3개 이미지 자산을 완전히 영구 삭제하고, 로컬 깃 훅(`.git/hooks/pre-push`) 내의 아키텍처 다이어그램 일관성 체크(Python 의존성) 루틴도 완벽히 소거하여 리포지토리의 경량화 및 불필요한 유지보수 오버헤드를 제거함.

### 브라우저 파비콘(Favicon) 디자인 고도화 (Step 38) - 완료
- **해결 내역**:
  - **디자인 세련화**: 기존의 무디고 투박하던 직교 2x2 테두리 형태의 파비콘 설정을 전면 소거함.
  - **아이덴티티 동기화**: 홈페이지 메인 로고의 프리미엄 감각을 정밀 재현하기 위해 45도 회전된 다이아몬드(마름모) 2x2 그리드 배열, 파란색-보라색 그라데이션, 그리고 은은한 네온 발광을 모사한 `<filter>` 수치(stdDeviation)를 결합한 SVG 코드로 완벽하게 리디자인 완료함.
  - **테마 동화 극대화**: 어두운 사각형 배경 가드를 제거하고 완전한 투명 배경 처리를 입혀, 브라우저가 라이트/다크 모드로 토글되더라도 로고 심볼이 일그러짐 없이 수려하게 떠 있도록 시각 품질을 일괄 조치함.
  - **레포지토리 대표 카드(Social Preview) 자산 제작**: GitHub 레포지토리 전용 1280x640 크기(2:1 비율)의 프리미엄 소셜 프리뷰 카드 이미지(github_social_preview.png)를 HTML/CSS 코드로 정밀 렌더링하고 스크린샷 캡처본을 확보하여 [github_social_preview.png](../docs/assets/github_social_preview.png) 경로에 적재 완료함.
  - **로컬 개발 가이드라인 보완 및 단일화**: 기존의 DB 격리 수동 3단계 기동 가이드 오타(`docker-compose.local.yml`)를 실제 파일명으로 일치 조치하고, 단 한 줄의 명령(`docker compose up --build`)만으로 DB-API-Client 전 서비스 삼각 편대를 로컬 컨테이너망 상에서 한방에 빌드 및 기동하도록 추천 1옵션을 설계하여 README.md의 편의성을 개선함.
  - **README.md 문서 제목 구조화 및 & 연결자 소거**: 대제목 및 소제목들에 무분별하게 남발되어 가독성을 저해하던 엔드(`&`) 연결 제목 14개소를 단일 명확한 키워드(예: `Infrastructure`, `Observability`, `CI/CD`, `AI Engineering`, `Appendices` 등)로 명료하게 함축 및 교정하여 문서 시각 품질을 극대화함.

---

## 2. 다음 목표 (Next Goals)
- **배치 주기별 AI 퍼즐 자동 생성 경과 관찰**: 04:17 AM 크론탭 실행 시 30x30 및 각 그리드별 데일리 퍼즐 생성이 파싱 에러 없이 매끄럽게 수행되는지 추가 모니터링 수행.
- **격리된 환경 안정성 모니터링**: 개별 VPC 대역에서 구동 중인 Staging/Production 각 서비스의 동작 이상 유무 및 데이터베이스 접속, 네트워크 보안 그룹 연동 정합성 관찰.
- **주기적인 백업 및 도커 프룬 작동 확인**: 백업 및 도커 청소 크론탭 스케줄러가 예정된 시간에 정상 트리거되어 리포지토리에 영향 없이 잘 구동되는지 로그 확인.
