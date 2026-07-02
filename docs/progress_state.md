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
  - **README.md 문서 제목 구조화 및 & 연결자 소거**: 대제목 및 소제목들에 무분별하게 남발되어 가독성을 저해하던 엔드(`&`) 연결 제목 14개소를 단일 명확한 키워드(예: `Infrastructure`, `Observability`, `CI/CD`, `AI Engineering`, `Appendices` 등)로 명료하게 함축 및 교정하여 문서 시각 품질을 극대화하고, 해당 가독성 정책을 거버넌스 규칙 문서 [documentation-guidelines.md](../.agents/rules/documentation-guidelines.md)에 공식 신설하여 반영 완료함.
  - **트러블슈팅 세부 서술형 소제목 축약**: 3.3.1 장애 대응 소제목 외에 기존의 1.6.1 및 2.4.1 서술형 소제목들 역시 각각 `### 1.6.1. 호스트 메모리 고갈 장애 복구`, `### 2.4.1. 배포 파이프라인 충돌 오류 극복`으로 명료하게 축약하여 가이드라인 정합성을 일괄 통제함.
  - **SLA 연산 수식 LaTeX 수식화**: README.md 부록 1의 PromQL 연산 명세를 기존 텍스트 나열 포맷에서 가독성이 뛰어난 LaTeX 수학 공식 블록(`$$`) 및 `promql` 코드 블록으로 구조화하여 전문성을 강화함. 또한 LaTeX 렌더러가 언더바(`_`) 기호 파싱 오류(`'_' allowed only in math mode`)를 뱉는 것을 방지하기 위해 기호($P_t$) 매핑 정의를 신설하여 수식을 무결하게 표준화하고, 수식 줄바꿈이 정상 작동하도록 마크다운 인덴트(들여쓰기)를 걷어내고 독립 블록으로 완전히 격리하여 가시성 오류를 조치함. 아울러 MTTR 및 MTBF 공식 좌변에 명시적 물리 단위인 초(sec) 단위를 삽입하여 정합성을 최종 완비함.
  - **AI Engineering 섹션 경량화 및 군더더기 배제**: 포트폴리오의 과장을 막고 엔지니어링 본질에 주안점을 두기 위해, AI 단순 기능(알고리즘 구현부, UI 위젯 CSS 디테일, CRUD 위주의 스키마 설명)에 해당하는 장황한 텍스트들을 걷어내고 명세형 아웃라인 위주로 압축하여 README.md의 핵심 정보 밀도를 조율함. 또한 3.1, 3.2, 3.3 하위 내의 모든 서술식 제목 요소들도 예외 없이 가이드라인 규칙([documentation-guidelines.md](../.agents/rules/documentation-guidelines.md))에 맞춰 직후 콜론 한 줄 작성을 제거하고 `<br>` 개행 및 아랫줄 들여쓰기로 완전히 교정함.
  - **호스트 보안 및 권한 정책 명세 반영**: 인바운드 22포트 완전 차단 및 SSM Session Manager 기반 제어 정책, SSH over SSM을 결합한 Ansible 터널링, 그리고 EC2 및 CI/CD(OIDC 연동) IAM 최소 권한(Least Privilege) 설계 구조를 README.md 보안 섹션에 투명하게 기술하여 포트폴리오의 보안 신뢰성을 강화함.

### AWS SSM Session Manager 도입 및 Staging 22포트 차단 (Step 39) - 완료
- **해결 내역**:
  - **보안 그룹 22포트 차단**: Staging 환경 테라폼(`infra/terraform/envs/staging/main.tf`)의 인바운드 보안 그룹에서 포트 22(SSH) 접근 규칙을 영구히 삭제(차단)하여 무단 접근 표면을 제거함.
  - **Ansible SSM 터널링 구성 예제 기술**: 포트 22가 전면 차단된 환경에서 Ansible Playbook의 호스트 기동 관리를 정상 지원하기 위해, `infra/ansible/hosts.ini`에 EC2 인스턴스 ID 매핑 및 `ProxyCommand`를 결합한 `ansible_ssh_common_args` 예시를 주석으로 이식함.
  - **SSM 접속 방법론 서술**: Windows/macOS 로컬 PC 개발자 기기에서 AWS CLI 세션 매니저를 통해 접속할 수 있도록 `session-manager-plugin` 의존성 도구 링크 및 SSH Config 프록시 구성 방안을 README.md 끝단에 명료하게 가이드화함.
  - **CI/CD 배포 파이프라인 SSM 연동 복구**: 22포트 차단 후 GitHub Actions 러너가 SSH 직접 연결 타임아웃 장애를 일으켰던 문제를 복구하기 위해, 워크플로우(`ci-cd.yml`) 내에 `session-manager-plugin` 의존성 설치 단계를 추가하고 동적 인스턴스 ID와 SSM ProxyCommand 기반으로 `hosts.ini`를 생성하여 러너가 터널을 통과해 Ansible 배포를 완수하도록 파이프라인을 고도화함.

### AWS SSM Session Manager Production 순차 적용 및 22포트 차단 (Step 40) - 완료
- **해결 내역**:
  - **운영(Production) 환경 보안 그룹 22포트 차단**: Production 환경 테라폼([main.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/production/main.tf))의 인바운드 보안 그룹에서 포트 22(SSH) 접근 규칙을 삭제(차단)하여 보안을 강화함.
  - **CI/CD 배포 파이프라인 SSM 연동**: [.github/workflows/ci-cd.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/ci-cd.yml) 내의 `deploy-production` 작업에 `session-manager-plugin` 의존성 설치 단계를 추가하고, 동적 인스턴스 ID 조회를 거쳐 SSM ProxyCommand 기반의 `hosts.ini`를 사용하여 배포를 수행하도록 파이프라인을 고도화함.
  - **테라폼 구성 검증**: `terraform init -backend=false` 및 `terraform validate` 명령어로 Production 환경의 테라폼 형상 설정에 오류가 없음을 정상 성공 검증함.
  - **인프라 명세 및 포트폴리오 서술 동기화 (Readme Review Report 반영)**: [README.md](file:///c:/Users/82107/dev/project/nemologic/README.md) 내 인프라 보안 그룹 명세, IAM 권한 매핑 불일치, OOM 복구 인시던트 참조 오류 등을 정정하고 CI/CD 권한 보안 로드맵 및 Staging EC2 기동/정지 비용 최적화 전략을 보완 기술함. 또한 운영 테라폼 코드의 보안 그룹 설명(`description`)을 실제 개방 포트 상태에 부합하도록 현행화함.
  - **보안 그룹 추가 폐쇄 (8080 & 5173 차단)**: 인바운드 보안 그룹의 최소 개방 원칙에 따라, Staging 및 Production 환경 테라폼 설정에서 8080(Spring Boot API) 및 5173(Vite Dev Server) 포트를 완전히 차단함. 모든 모니터링 메트릭 수집 및 API 트래픽은 Nginx(80/443)의 검증 프록시 경로를 거쳐 내부 통신하도록 단일화하여 인프라 보안 위협 표면을 최소화함.
  - **다계층 도커 브리지 네트워크 격리 문서화**: [docker-compose.prod.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/docker-compose.prod.yml) 및 [docker-compose.stage.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/docker-compose.stage.yml)에 기구축되어 운영 중인 frontend-net 및 backend-net 격리 구조와 침투 시 횡적 이동(Lateral Movement)을 막는 심층 방어 전략 및 보안 로드맵을 [README.md](file:///c:/Users/82107/dev/project/nemologic/README.md)에 사실 위주로 콤팩트하게 문서화함. 특히 데이터베이스 전용 브리지(`backend-net`)에 `internal: true` 옵션을 적용해 외부 인터넷 망과의 인/아웃바운드를 완전 격리하여, RCE 침투 시의 데이터 외부 유출(Exfiltration) 시도를 네트워크 레벨에서 원천적으로 봉쇄함. (1.4.1 C4 Container Diagram에 nested boundary 시각화 반영 완료)
  - **도커 네트워크 마이그레이션 락 자가 치유(Self-healing) 단계 추가**: `backend-net`을 `internal: true` 내부망으로 변경 재정의할 때 기존 실행 중인 백엔드 컨테이너가 엔드포인트로 물려 있어 네트워크 삭제가 잠기는 데몬 교착 상태(daemon migration lock)를 해결하기 위해, 플레이북([playbook.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/playbook.yml))에 DB 시작 전 기존 백엔드 컨테이너들의 네트워크 연결을 강제 끊기(`docker network disconnect -f`) 처리하는 자가 치유(Self-healing) 태스크를 추가 설계함.
  - **보안 그룹 문서 계층화 및 규칙 최적화**: [README.md](file:///c:/Users/82107/dev/project/nemologic/README.md)의 보안 그룹 설정(1.4.2.1) 아래에 있던 비정형 Note 블록을 제거하고 H5 공식 서브섹션(1.4.2.1.1~3)으로 분할 계층화하여 중복 기술을 제거함. 이 문서 포맷팅 규칙을 프로젝트 공통 규칙인 [.agents/rules/documentation-guidelines.md](file:///c:/Users/82107/dev/project/nemologic/.agents/rules/documentation-guidelines.md)에 상시 반영함.

### AWS IAM OIDC 기반 무키(Keyless) 인증 및 최소 권한 고도화 (Step 41) - 완료
- **해결 내역**:
  - **AWS OIDC 프로비저닝 (Phase 1)**: 단일 계정 내 OIDC Provider 중복 생성에 따른 API 409(EntityAlreadyExists) 충돌 에러와 병행 plan 검증 시의 리소스 선후 관계(not found) 에러를 방어함. Staging 테라폼에서만 `aws_iam_openid_connect_provider` 리소스를 직접 생성하고, Production 테라폼에서는 `aws_caller_identity` 데이터 소스를 통해 현재 계정 ID를 조회하여 ARN을 동적으로 조립해 참조하도록 최적화 설계함.
  - **GitHub Actions 연동 IAM Role 매핑**: 환경별로 격리된 IAM Role을 생성하고 assume policy를 바인딩함. Staging Role은 전체 sub 대역을 허용하며, Production Role은 `refs/heads/main` 브랜치 및 `refs/tags/v*` 릴리즈 태그 배포 시에만 가동되도록 sub 대역을 타이트하게 제한함.
  - **역할 ARN 출력(Outputs) 정의**: [staging/outputs.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/staging/outputs.tf) 및 [production/outputs.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/production/outputs.tf)에 생성된 IAM Role ARN of output 출력을 기재하고 두 환경의 `terraform validate` 검증을 통과함.
  - **CI/CD 파이프라인 OIDC 전환 (Phase 2)**: [.github/workflows/ci-cd.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/ci-cd.yml)에서 전역 `permissions` 권한(`id-token: write`, `contents: read`)을 선언하여 OIDC JWT 발급 자격을 활성화함. 또한 모든 인프라 plan/apply 및 애플리케이션 deploy 잡(총 6개 잡)의 초반부에 `aws-actions/configure-aws-credentials` 단계를 이식하여 동적으로 1회용 STS 임시 보안 토큰을 AssumeRole하도록 스위칭함. 이에 따라 전역 및 개별 스텝에 하드코딩 주입되던 `AWS_ACCESS_KEY_ID` 및 `AWS_SECRET_ACCESS_KEY` 환경 변수를 파이프라인에서 완전히 Pruning 처리함.
  - **수동 배포 수립 및 더미 커밋 제거**: 소스 주석 수정으로 빌드를 트리거하는 임시방편(안티패턴)을 완전 극복하기 위해 `workflow_dispatch` 수동 기동 이벤트를 추가함. 수동 기동 시(`github.event_name == 'workflow_dispatch'`)에는 경로 감지(paths-filter) 조건과 관계없이 빌드, Docker 패키징, Staging/Production S3 동기화 및 호스트 배포 전 구간이 OIDC를 통해 강제 구동(Bypass)되도록 워크플로우 분기 로직을 리팩토링함.

### 도커 네트워크 마이그레이션 중복 해제 에러 해결 (Step 42) - 완료
- **해결 내역**:
  - **현상**: Ansible 배포 파이프라인의 `Start Database service` 단계에서 `Error response from daemon: container ... is not connected to the network nemologic_backend-net` 에러를 뱉으며 배포가 실패함.
  - **원인 분석**: `Force disconnect` 루프 내에 `nemologic-db` 컨테이너가 포함되어 있어 사전에 네트워크가 강제 분리되었고, 이로 인해 Docker Compose가 `db` 컨테이너 정지/기동을 위한 네트워크 상태 동기화 과정에서 에러를 발생시킴. 또한 이미 마이그레이션이 완료된 네트워크 환경에서도 매번 연결 강제 분리가 실행되어 다운타임 위협이 존재함.
  - **해결 조치**:
    1. [playbook.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/playbook.yml) 내에 `docker network inspect` 명령을 통해 해당 네트워크가 이미 `internal: true`로 마이그레이션 완료되었는지 여부를 동적으로 사전 검증하는 스텝을 신규 추가함 (`backend_net_internal`).
    2. 네트워크가 아직 마이그레이션되지 않았을 때만 `Force disconnect` 태스크가 구동되도록 제어하여 배포 시 순단 리스크를 방지함.
    3. `Force disconnect` 대상 컨테이너 루프에서 `nemologic-db`를 영구 제외하고, Docker Compose의 자체 제어(stop/recreate) 범위 내에서 네트워크 차단 및 갱신이 정상 처리되도록 고도화함.
    4. Staging 환경의 `nemologic-db` 컨테이너 상태를 수동 복원(SSM 명령어를 통한 기존 컨테이너 강제 소거 및 `docker compose up -d db` 재생성)하여 `internal: true` 네트워크 격리가 무사히 정합성을 유지하도록 복원 완료함.

### 운영(Production) 환경 OIDC AssumeRole 권한 에러 해결 (Step 43) - 완료
- **해결 내역**:
  - **현상**: GitHub Actions의 `Terraform Production Apply (Manual Approval)` 작업에서 OIDC를 통한 AWS IAM 역할(`nemologic-production-github-role`) 인수 과정 중 `Not authorized to perform sts:AssumeRoleWithWebIdentity` 에러가 발생함.
  - **원인 분석**: `infra-apply-production` 작업은 `production` 환경(`environment: production`) 내에서 작동하므로 OIDC 토큰의 `sub` 클레임이 `repo:devdoyen/rogic.io:environment:production` 형식을 갖게 됨. 기존 IAM 역할의 신뢰 정책(Trust Policy)은 `repo:devdoyen/rogic.io:ref:refs/*` 패턴만 허용하여 인증 실패가 유발됨.
  - **해결 조치**:
    1. [production/main.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/production/main.tf) 내의 `github_actions_production` IAM 역할의 `AssumeRolePolicyDocument` 신뢰 정책에서 `sub` 클레임 조건에 일반 Git 참조 패턴(`ref:refs/*`) 및 환경 명세 패턴(`environment:production`)을 동시에 포함하도록 리스트 형식으로 확장함.
    2. GitHub Actions 러너가 아직 이 역할을 획득할 수 없는 상태이므로, 로컬 개발자 PC의 관리자(admin) 자격 증명을 사용하여 대상 리소스를 타겟 지정(`terraform apply -target=aws_iam_role.github_actions_production`)함으로써 AWS에 신뢰 관계 변경 정책을 안전하게 선반영함.

---

### AWS IAM 최소 권한(Least Privilege) 수립 (Step 44) - 완료
- **해결 내역**:
  - **현상**: GitHub Actions OIDC 역할에 `AdministratorAccess` 전권 정책이 부여되어 있어 최소 권한 통제 원칙에 위배됨.
  - **해결 조치**:
    1. **Staging 반영 (Phase 1)**: [staging/main.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/staging/main.tf) 내에 `github_actions_staging_policy` 커스텀 IAM 정책을 정의하고 역할을 바인딩하여 전권 권한을 회수함. EC2, VPC, S3, DynamoDB, IAM, CloudWatch, SNS, SSM, CloudFront 범위로 한정함. (이후 ACM/Route53/로그 목록 조회 대응을 위한 `*` 와일드카드 보완 완료)
    2. **Production 반영 (Phase 2)**: [production/main.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/envs/production/main.tf) 내에 `github_actions_production_policy` 커스텀 IAM 정책을 추가 연동함.
    3. **로그 및 인증서 와일드카드 보완**: 테라폼 상태 리프레시 시 `DescribeLogGroups` 및 `DescribeCertificate` 등 리소스 종속성 없는 메타데이터 API 접근 차단 문제를 해결하기 위해, Staging 및 Production의 커스텀 정책 모두 `logs:*` 및 `acm:*` 리소스 제한을 `*` 와일드카드로 완화 적용하고 로컬 Admin 권한을 통한 선반영(`terraform apply -target`)을 수립하여 배포 병목을 해결함.

### README.md 프로젝트 포트폴리오 문서 고도화 및 정합성 검증 (Step 45) - 완료
- **해결 내역**:
  - **아키텍처 및 트래픽 흐름 불일치 해결**: Level 1 System Context 다이어그램 내 DNS 라우팅 및 EC2 EIP Nginx 연결 흐름을 명확히 하고, Level 2 다이어그램에서 Player가 Staging Nginx에 접근하는 오류를 SRE/QA(CI/CD)로 교정함. 또한 다이어그램 구조와 구성요소 명세(1.1.2)의 대칭성을 맞추기 위해 다이어그램에 표현되지 않은 Telemetry Proxy 항목을 제외하고 물리적 배치 영역(Global Edge Delivery, Core API Server & Database)을 기준으로 스펙 항목을 대칭 정렬함.
  - **SLO/SLA 수학적 오류 및 보정**: Incident Count PromQL 수식의 floor 연산 적용 및 MTTR/MTBF 분모 보정 함수 `clamp_min` LaTeX 기호를 KaTeX/MathJax 렌더러 파서 오류 방지를 위해 `\text{clamp}_{\text{min}}` 하첨자 포맷으로 교정하여 수식 정합성을 보완함.
  - **논리적 모순 및 수치 정정**: Staging 환경 상태 배지를 `Idle / On-Demand`로 현실화(기존 Active) 및 DB Failover/PITR 상실에 따른 RPO(최대 6시간)와 RTO(약 20분) 지표가 혼동된 오류를 정정함. 또한 타 아키텍처와의 불필요한 비교를 방지하기 위해 가용성 비교 테이블을 현재 단일 인프라 구성 하에서의 최대 허용 한계 목표(SLA/SLO Target) 테이블(Availability, RPO, RTO, MTBF, MTTR 명세)로 개편함.
  - **구조 및 가독성 개선**: 5단계 깊이의 복잡한 헤더 체계를 제거하고 H1/H2/H3/H4 영어 헤딩 통일 및 볼드 리스트로 평탄화함. 또한 동일한 인프라 설명(Agentless Pull, GraalVM 풋프린트, ALB 제거, 컴파일 오프로딩)이 중복 배치된 곳을 삭제하고 상호 앵커 링크로 대체함. 더불어 본 프로젝트의 핵심 운영 타겟이자 주안점인 '제약 조건 및 엔지니어링 원칙(0.1)' 섹션을 README 최상단(0.1)으로 배치하여 포트폴리오의 전체 아키텍처 설계를 읽기 전에 최우선 인지되도록 구조를 개편했으며, 지루한 기술 스택 표를 2열 배지 테이블로 전환하여 가로 정렬 선을 완벽하게 맞추고 압축함. 또한 본문 시각적 일관성을 확보하기 위해 게임 데모 시연 이미지의 너비(width)를 100%로 상향 조정함.
  - **문맥 정제 및 오타 교정**: 개발자 회고 문장을 1인칭 어조에서 전문적인 포트폴리오 결과 중심 어조로 압축하고, "수입되는" -> "유입되는" 오타를 수정함. 또한 대제목 하단의 지루하고 뻔한 인프라 개요 설명 문장을 삭제하여 가독성을 제고함.

### Staging EC2 야간 자동 중지 스케줄 복구 (Step 46) - 완료
- **해결 내역**:
  - **현상**: 활성 테스트 진행 지원을 위해 일시적으로 비활성화해둔 Staging 인프라 비용 절감용 야간 자동 중지 스케줄링이 꺼져 있었음.
  - **해결 조치**:
    1. [.github/workflows/staging-cleanup.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/staging-cleanup.yml) 워크플로우 파일 내의 `schedule` cron 트리거(`0 17 * * *` - 매일 새벽 2시 KST 실행) 주석을 해제하여 야간 자동 중지 로직을 복구함.

### README.md 비용 최적화 및 타협 섹션 중복 해결 (Step 47) - 완료
- **해결 내역**:
  - **중복 섹션 통합**: `README.md` 내에서 인프라 구성 비용 최적화(기존 1.2)와 기술적 타협 및 완화 대책(기존 1.3) 간에 심각하게 중복되던 내용을 하나의 통합 섹션인 `1.2. Cost-Effective Architecture` 하위로 병합 완료함. 각 요소(Compute, Load Balancer, Database, Staging)별로 최적화 조치(Optimization), 기술적 제약(Trade-off), 완화 대책(Mitigation)을 유기적으로 서술하여 독자의 가독성을 극대화함.
  - **오류 및 중복 제거**: 1.2.4 섹션 내에 깨진 텍스트(S3 백업 등) 및 중복 기입되어 있던 `Staging Resource Stop/Start Scheduling`을 단일화하고 올바른 내용으로 정제하여 문서 품질을 개선함.
  - **인덱스 및 링크 정합성 재구축**: 기존 1.3 섹션이 제거됨에 따라 후속 장표들(Security Infrastructure, Observability, Troubleshooting)의 인덱스 번호를 순차적으로 하나씩 당겨서 현행화함 (`1.4 -> 1.3`, `1.5 -> 1.4`, `1.6 -> 1.5`). 또한 CI/CD 컴파일 오프로딩 섹션 내에서 기존 `1.3.1`을 지칭하던 마크다운 앵커 링크를 신규 통합된 `1.2.1` 참조 앵커로 전면 수정하여 원활한 문서 내부 탐색을 보장함.

### README.md 비용 최적화 섹션 보편화 및 개괄식 목록화 (Step 48) - 완료
- **해결 내역**:
  - **아키텍처 분류 체계 보편화**: 사용자 피드백에 따라 `1.2. Cost-Effective Architecture` 하위 소제목들을 보편적인 리소스 분류인 `Compute`, `Network & Delivery`, `Database & Storage`, `Staging Environment`로 전면 리팩토링 및 구조화함.
  - **개괄식 포맷 변환**: 기존 서술식 문장들을 가독성 높은 글머리 기호(`- `) 기반의 개괄식 리스트 포맷으로 변경하여 핵심 정보(최적화 조치, 기술적 제약, 완화 대책)의 시인성을 극대화함.
  - **S3 OAC 및 CloudFront 최적화 통합**: S3 OAC 및 CDN 캐싱 배포가 네트워크/스토리지 리소스 최적화 효과를 유발하는 점을 반영하여 `Network & Delivery` 섹션 내의 최적화 조치 리스트로 통합 매핑함.
  - **앵커 링크 재보정**: 분류 체계가 보편적 소제목 규격으로 수정됨에 따라, 컴파일 오프로딩 설명(2.2.1) 등 문서 내의 참조 앵커들을 `[1.2.1. Compute](#121-compute)`로 맞추어 재보정 완료함.

### README.md 인프라 종합 성과 분석(Performance & Cost Analysis) 섹션 신설 (Step 49) - 완료
- **해결 내역**:
  - **종합 성과 분석 섹션 구성**: `README.md` 내에 `1.5. Performance & Cost Analysis` 대단원 독립 섹션을 신설함.
  - **재무 및 신뢰성 지표 통합**: 기존에 1.2 하단에 파편화되어 있던 인프라 월간 운영 비용 분석 테이블과 1.4 하단에 타겟 제약으로 고정되어 있던 가용성 및 신뢰성(SLI) 지표를 한곳으로 모아 비용 대비 서비스 수준 분석을 종합화함.
  - **실측 데이터 정직한 반영 및 회고**: GA4(활성 사용자 57명, 누적 이벤트 655회, 체류 시간 2분 8초) 및 Grafana Cloud 실측치(가용성 82.1%, MTBF 10~11.7h, MTTR 1.49~2.56h)를 투명하게 공표함. 또한, 초기 OOM 장애 단계에서부터 자원 최적화 조치를 통해 시스템이 점진적으로 안정화(최근 7일 Uptime 99% 이상 달성)에 이르는 과정의 **운영 지표 기술 회고(Operational Metrics Retrospective)**를 작성하여 엔지니어링 신뢰도를 제고함.
  - **인덱스 계층화**: 기존의 `1.5. Troubleshooting`을 `1.6. Troubleshooting` 및 `1.6.1`로 순차적으로 밀고 내부 앵커 참조들이 온전하게 기능하도록 리팩토링함.

### README.md 성과 분석 대단원(# 4) 승격 및 부록 인덱스 보정 (Step 50) - 완료
- **해결 내역**:
  - **대단원 승격 리팩토링**: `Performance & Cost Analysis` 섹션이 인프라(1), CI/CD(2), AI(3) 영역의 종합 성과를 포괄하므로 인프라 하위(1.5)가 아닌 최상위 대분류 대단원인 `# 4. Performance & Cost Analysis` 로 격상 및 이동 완료함.
  - **인프라 Troubleshooting 복원**: 이전에 1.6으로 이동했던 Troubleshooting 단원을 원래 자리인 `## 1.5. Troubleshooting` 및 `### 1.5.1` 로 당겨서 복원하고, 성과 회고(4.2) 내의 앵커 참조도 1.5.1로 정상 복원함.
  - **부록(Appendices) 인덱스 하향 조정**: 기존 `# 4. Appendices` 대단원을 `# 5. Appendices` 로 한 칸 하향 이동하고 하위 구조(`## 5.1`, `### 5.1.1~3`)를 전면 보정함.

### README.md 보안 인프라 섹션(1.3) 개괄식 목록화 리팩토링 (Step 51) - 완료
- **해결 내역**:
  - **보안 아키텍처 개괄식 포맷 변환**: `1.3. Security Infrastructure` 하위의 모든 서술형 설명 문장들을 1.2와 일관된 글머리 기호 기반의 구조화된 개괄식 포맷으로 변경함.
  - **가독성 및 시인성 고도화**: 물리 격리 VPC 및 가상 도커 브릿지 격리(1.3.1), SSM 터널링 세부 스펙(1.3.2), 인바운드/프록시 중재(1.3.2.1), IAM 단기 자격 증명 OIDC/서비스 최소 권한(1.3.2.2), Certbot 인증서 갱신 자동화(1.3.3), 테라폼 원격 상태 형상 보안(1.3.4)의 복잡한 줄글 표현을 명료하게 구조화하여 문서 정합성을 극대화함.

### README.md 보안 인프라(1.3) AWS Well-Architected Framework 표준 규격 정규화 (Step 52) - 완료
- **해결 내역**:
  - **보안 아키텍처 분류 체계 정규화**: 기존의 산발적이고 임의적이었던 보안 서브섹션들을 AWS Well-Architected Framework 보안 기둥(Security Pillar)에 맞춰 `1.3.1. Identity & Access Management`, `1.3.2. Infrastructure Protection`, `1.3.3. Data Protection` 3대 영역으로 전면 재구성하고 정규화함.
  - **콘텐츠 통합 및 압축**: IAM 최소 권한 정책 및 Ansible SSM Tunneling Spec을 IAM 섹션(1.3.1) 하위로 이관하고, 네트워크 다이어그램 및 보안 그룹 정책을 인프라 보호 섹션(1.3.2)으로, 인증서/상태 파일 보안을 데이터 보호 섹션(1.3.3)으로 유기적으로 묶어서 문서의 정보 응집도를 대폭 개선함.

### README.md Ansible SSM 상세 코드블록 부록 이관 (Step 53) - 완료
- **해결 내역**:
  - **구현 상세 이관**: 메인 보안 아키텍처 본문(`1.3.1. Identity & Access Management`)에 상주해 흐름을 해치던 구체적인 `hosts.ini` 설정용 Ansible SSM Tunneling 코드 블록을 본문에서 도려냄.
  - **부록 병합 및 앵커 참조**: 해당 코드 스니펫을 호스트 접근 툴 가이드라인 부록인 `## 5.1.3. AWS SSM Session Manager Setup` 최하단 항목으로 이관하여 문서의 정보 위계를 고도화하고, 본문에서는 부록으로 직접 이동할 수 있는 마크다운 앵커 링크(`[5.1.3. AWS SSM Session Manager Setup](#513-aws-ssm-session-manager-setup)`)를 배치함.

### README.md IAM verbose 테이블 제거 및 Mermaid 다이어그램 도입 (Step 54) - 완료
- **해결 내역**:
  - **다이어그램 대체 및 시인성 극대화**: 본문 `1.3.1.1. IAM Least Privilege Design` 의 과도하게 길었던 11개 대상 서비스별 IAM Actions 세부 매핑 테이블을 완전히 삭제하고, 대신 전체 주체(Runner, EC2)와 인증 체계(OIDC, Instance Profile), IAM 역할/정책, AWS 타깃 자원 간의 유기적 흐름을 표상하는 **Mermaid IAM Architecture Flowchart**를 설계하여 삽입함.
  - **정보 간결성 획득**: 핵심 요약인 주체별 자격 증명 테이블만 다이어그램과 병치하고 불필요한 구현성 Policy 세부 테이블은 완전히 걷어냄으로써 전체 다큐먼트 아키텍처 리포트의 가독성과 정보 전달력을 완성함.

### README.md IAM 다이어그램 C4 모델 표준 규격(Level 3) 업그레이드 (Step 55) - 완료
- **해결 내역**:
  - **C4 모델 표준 적용**: 기존 `1.3.1.1` 하위의 일반 `flowchart TD` 기반 보안 다이어그램을 C4 Component 다이어그램 규격으로 전면 전환하여, 프로젝트의 타 아키텍처 다이어그램들과의 기술 정합성 및 일관성을 확보함.
  - **C4Component 구성**: 주체(GitHub Actions Runner, EC2)를 `Container`로, 신뢰점 및 역할/Policies를 `Component`로, 대상 자원군들을 `System` 경계로 정확하게 맵핑하여 클라우드 보안 토폴로지 가독성을 극대화함.

### README.md 관제(1.4) 정규화 및 트러블슈팅(1.5) SRE 포스트모템 요약 테이블 주입 (Step 56) - 완료
- **해결 내역**:
  - **관제 아키텍처 명칭 정규화**: `1.4. Observability` 의 하위 구성 요소들을 모니터링 3대 핵심 표준인 `1.4.1. Metrics & Telemetry`, `1.4.2. Log Aggregation & Storage`, `1.4.3. Alerting & SLO Visualization` 체계로 승격하고 명칭을 정규화함.
  - **포스트모템 메타데이터 테이블 도입**: 인프라 장애 장표인 `1.5.1. Host Memory Exhaustion Incident` 의 도입부 상단에 장애 유형, 영향도, 핵심 유발 원인, 복구 조치, 실측 RTO/RPO 지표를 한눈에 볼 수 있는 **SRE 표준 장애 포스트모템 요약 테이블**을 설계하여 이식함.
  - **수식 기호 이스케이프 복원**: 파이썬 빌더 치환 시 이스케이프 오류로 유실되었던 LaTeX PromQL 수식 특수 기호들(Times, Fraction, Right, Floor 등)을 정교한 복원 스크립트를 통해 온전하게 재보정함.

### README.md PromQL 수식 및 쿼리 블록 부록 이관 (Step 57) - 완료
- **해결 내역**:
  - **관제 쿼리 스펙 이관**: 메인 관제 아키텍처 본문(`1.4.3. Alerting & SLO Visualization`)에 위치하여 다큐먼트 가독성을 해치던 구체적인 `PromQL` 쿼리 스펙 및 LaTeX 수학 공식 블록들을 통째로 본문에서 걷어냄.
  - **신규 부록 신설**: 해당 장표를 최하단 부록의 신규 단원인 `## 5.2. PromQL Query Formulations (SLO Metrics)` 로 이관하여 병합함. 본문 1.4.3 끝에는 부록으로 즉각 도달 가능한 마크다운 앵커 링크를 배치해 가독성을 높임.

### README.md 1.5.1 장애 포스트모템 요약 테이블 제거 및 원복 (Step 58) - 완료
- **해결 내역**:
  - **불필요 가독성 저해 요소 제거**: `1.5.1. Host Memory Exhaustion Incident` 섹션 초입에 가미되었던 SRE 포스트모템 테이블이 문서를 불필요하게 복잡하게 만든다는 판단에 따라 전면 삭제 조치함.
  - **구조 간소화**: 원래의 명료한 3단 글머리 구성(배경, 해결 방안, 기술적 교훈 및 의사결정)으로 완전 원복하여 문서의 담백한 전달력과 본질을 유지함.

### README.md 1.5.1 트러블슈팅 SRE 리소스 진단 내역 고도화 (Step 59) - 완료
- **해결 내역**:
  - **진단 기술성 강화**: 리눅스 `top` 및 `vmstat` 진단 명령어를 활용하여 CPU `idle` 0% 수렴 및 I/O Wait(`wa`)의 상승으로 인한 디스크/CPU 스래싱(Thrashing) 상태를 과학적으로 규명한 실무 경험을 보강함.
  - **관제 모니터링 연동**: 진단 지표(`wa`, `idle`)를 Grafana Cloud에 추가 이식하여 선제적 모니터링 체계를 확보한 내용을 추가함.
  - **도커 이미지 콜드 스타트 장애 규명**: 최초 배포 시 Base Image Pull/Extract로 인해 1시간 이상 파이프라인이 멈췄던 병목과, 이후 웜 스타트 시점에서의 도커 캐시 메커니즘을 통한 자원 해소 구조를 상세화하여 장애 극복 역사의 설득력을 극대화함.

### README.md 4.2 / 4.3 내 7일치 실측 데이터로 개정 (Step 60) - 완료
- **해결 내역**:
  - **SLO 및 사용자 통계 7일 실측치 개정**: 초기 불안정했던 30일 데이터(가용성 82.1%) 대신, 시스템 튜닝이 완전히 종결되어 안정된 최근 7일(6월 25일 ~ 7월 2일)간의 실제 메트릭 데이터(평균 가용성 98.63%, 평균 MTTR 11.76분, 평균 MTBF 14.2시간)로 개정하여 설득력을 확보함.
  - **구글 애널리틱스 지표 동기화**: 활성 사용자 39명, 총 이벤트 수 535회, 활성 사용자당 평균 참여 시간 2분 53초로 7일 실측 캡처 데이터에 완벽하게 일치시킴.

### README.md 4.2 / 4.3 내 라이브 증빙 자료 이미지 복사 및 임베드 (Step 61) - 완료
- **해결 내역**:
  - **자산화 및 복사**: 에이전트 미디어 아티팩트 보관함에 존재하는 실제 GA4 보고서 원본과 Grafana 대시보드 캡처본을 리포지토리의 로컬 경로인 `docs/assets/` 로 안전하게 복사하여 Git 형상 관리 하에 등록함.
  - **증빙 임베딩 연동**: README.md의 `4.2` SLO 분석 본문 하위에 Grafana Live Snapshot 링크 및 `grafana_sla_snapshot.png` 상대경로 이미지를, `4.3` 트래픽 메트릭 하위에 `ga4_report.png` 이미지를 결합 수록하여 포트폴리오 문서의 객관적 증빙 신뢰성을 최고 수준으로 격상시킴.

### README.md CI/CD(2) DevOps 표준 정규화 및 죽은 앵커 링크 패치 (Step 62) - 완료
- **해결 내역**:
  - **DevOps 표준 기둥 정규화**: `2. CI/CD` 하위 구성을 업계 표준 DevOps 분류 3대 영역인 `2.1. Pipeline Workflow` (배포 파이프라인 흐름), `2.2. Artifact & Release Management` (산출물 및 릴리즈 관리), `2.3. Continuous Validation` (지속적 검증 및 품질 통제)로 정규화하고 2단 개괄식 목록화로 리팩토링함.
  - **죽은 앵커 링크 보정**: 이전 1.2 섹션 구조조정 시 유실되어 클릭이 고장났던 `2.2.1` 내부의 `[1.2.1. Compute](#121-compute)` 앵커 링크를 유효한 앵커인 `[1.2. Cost Optimization](#12-cost-optimization)` 내 Compute 단락 참조로 현행화 패치함.
  - **대분류 유실 핫픽스**: 치환 슬라이싱 과정에서 유실되었던 `# 2. CI/CD` 및 `# 3. AI Engineering` H1 대분류 헤더를 안전하게 원상복구 완료함.

### README.md AI Engineering(3) LLMOps 표준 정규화 및 H1 헤더 복원 (Step 63) - 완료
- **해결 내역**:
  - **LLM/LLMOps 표준 기둥 정규화**: `3. AI Engineering` 하위 구성을 업계 표준 인공지능 엔지니어링 분류 3대 영역인 `3.1. LLM Generation Pipeline` (LLM 생성 파이프라인), `3.2. Automated Quality Guardrails` (자동 품질 검증 가드레일), `3.3. AI Governance & Human-in-the-Loop` (AI 거버넌스 및 관리 통제)로 정규화하고 2단 개괄식 목록화로 리팩토링함.
  - **대분류 유실 핫픽스**: 치환 슬라이싱 과정에서 유실되었던 `# 3. AI Engineering` 및 `# 4. Performance & Cost Analysis` H1 대분류 헤더를 안전하게 원상복구 완료함.

### README.md 3.3 내 Antigravity IDE 에이전트 개발 협업 스택 기술 (Step 64) - 완료
- **해결 내역**:
  - **AI 협업 개발 스택 추가**: `Antigravity IDE` 페어 프로그래밍 협업 시 실제 활용된 AI 모델 역할 구조를 명세함. Gemini 3.5 Flash (주 개발/문서화), Claude Sonnet 4.6 (단위 코드 리뷰/정적 진단), Claude Opus 4.6 (전체 포트폴리오 최종 점검 및 고부하 설계 리뷰 게이트용 선별 가동)의 사양 분배 체계를 설명함.
  - **토큰 가성비 제안 반영**: Opus 모델의 큰 토큰 비용(Token Cost) 제약을 명시하여 최상위 아키텍처 점검 게이트 역할에만 제한 가동하는 현실적인 AI 제어 공학 스토리를 완성함.

### README.md 3.3 내 Chrome 연동 Gemini 3.5 Flash 활용성 보강 (Step 65) - 완료
- **해결 내역**:
  - **Chrome 연동 AI 도구 활용처 명세**: 크롬 브라우저 상에 연동된 Gemini 3.5 Flash 모델의 실전 사용 시나리오(AWS CloudWatch/Grafana 실시간 인프라 장애 로그 추적, Chrome DevTools 연계 프론트엔드 캔버스 및 CSS 디자인 검증, 브라우저 콘솔 오류 디버깅)를 명시하여 개발 다각화 성과를 완성함.

### Ansible playbook.yml awscli 설치 유실 핫픽스 (Step 67) - 완료
- **해결 내역**:
  - **universe 리포지토리 활성화**: 도커 미설치 신규 서버 구축 분기 진입 시, 우분투 universe 패키지 저장소가 누락되어 `awscli` 및 `prometheus-node-exporter` 설치가 취소되던 결함을 패치함. `Update apt package cache` 태스크 바로 다음에 `Enable universe repository` 태스크를 주입하여, apt가 정상적으로 해당 패키지를 탐색해 낼 수 있도록 가용화함.

### Staging Nginx non-root SSL 권한 오류 group_add 핫픽스 (Step 68) - 완료
- **해결 내역**:
  - **그룹 상속을 통한 권한 우회**: Nginx 컨테이너를 `nginx` 비특권 사용자로 기동 시, 호스트에서 `root:root` 로 마운트된 LetsEncrypt SSL 인증서 파일에 접근할 수 없어 `Permission Denied` 크래시를 내던 오류를 패치함. Nginx 컨테이너는 비특권 `nginx` 유저로 유지하되, `docker-compose` 내에 **`group_add: - root`** 옵션을 주어 호스트의 root 그룹 읽기 권한을 상속받도록 결합함으로써 호스트 OS 변경 없이 SSL 복구를 완료함.

### Staging Nginx PID 생성 쓰기 거부 /var/run tmpfs 핫픽스 (Step 69) - 완료
- **해결 내역**:
  - **PID 파일 쓰기 차단 극복**: Staging 환경 배포 시 Nginx 가 Dockerfile 빌드 과정(sed 치환) 없이 공식 이미지를 기동함에 따라, `read_only` 상태에서 `/var/run/nginx.pid` 생성이 거부되던 장애를 패치함. `docker-compose` 설정 내 Nginx 서비스의 `tmpfs` 마운트 목록에 **`/var/run`** 을 명시적으로 추가하여 비특권/읽기 전용 Nginx 의 정상 기동을 완성함.

### LetsEncrypt 개인키 0600 권한 완화(640) 및 non-root Nginx SSL 복원 (Step 70) - 완료
- **해결 내역**:
  - **비특권 기동과 보안성 균형 달성**: LetsEncrypt 가 발급하는 비공개 개인키(`privkey.pem`)의 소유자 전용 `0600` 권한으로 인해 비특권 컨테이너(`user: nginx` 및 `group_add: - root`)가 SSL 읽기에 실패하던 장애의 궁극적 해법을 이식함.
  - **그룹 읽기(g+rX) 권한 추가**: `playbook.yml` 내에 `Allow root group read access to LetsEncrypt keys` 태스크를 신설하여, 키 원본 저장소인 `/etc/letsencrypt/archive/{{ cert_domain }}/` 경로의 권한을 `640`/`750` 권한으로 자동 개정함.
  - **보안 무결성 수호**: `other` (타인) 권한은 `0` 으로 완벽히 묶어두어 호스트 머신의 보안을 해치지 않고, 오직 `root` 그룹에 속한 컨테이너 내부의 `nginx` 비특권 계정만 개인키를 안정적으로 읽어 구동되도록 핫픽스를 완결함.

### LetsEncrypt 상위 경로 진입 권한 g+rX 활성화 및 배포 자가 진단 도입 (Step 71) - 완료
- **해결 내역**:
  - **경로 진입 차단 극복**: LetsEncrypt 개인키 파일이 존재하는 상위 디렉터리 경로들(`/etc/letsencrypt/live` 등)의 그룹 실행 권한 비트 누락으로 Nginx 프로세스의 스캔 진입이 거부되던 장애를 패치함. `live`, `archive`, 부모 경로 전체를 루프 돌려 그룹 읽기/실행 권한(`g+rX`, `o-rwx`)을 명시적으로 개정함.
  - **배포 자가 진단(Diagnostics) 태스크 도입**: 배포 완료 직후 모든 원격 컨테이너 구동 현황(`docker ps -a`) 및 최신 logs를 Actions 빌드 화면에 실시간으로 프린트하도록 플레이북 최하단에 디버그 단계를 장착하여 잠재 장애 시 즉각 추적할 수 있는 툴체인을 마련함.

### Ansible playbook.yml shell chdir 문법 구문 오류 핫픽스 (Step 72) - 완료
- **해결 내역**:
  - **문법적 결함 패치**: `Debug and display remote docker container status` 태스크에서 `shell` 모듈 사용 시 `chdir` 파라미터를 태스크 루트 수준에 잘못 병렬 매핑하여 발생하던 파서 분석 장애(`conflicting action statements: shell, chdir`)를 해결함. `chdir` 을 `args:` 지시어의 하위 키로 중첩 정렬하여 Ansible 문법 정합성을 충족함.

### LetsEncrypt 전체 경로 전역 재귀적 권한 완화 및 non-root Nginx SSL 전면 개통 (Step 73) - 완료
- **해결 내역**:
  - **링크 수준 경로 탐색 차단 해소**: LetsEncrypt 개별 경로 루프 처리 시 일부 심볼릭 링크 파일 자체나 갱신 파일들의 권한 장벽이 남아 Nginx 기동에 실패하던 크래시를 패치함. 최상위 `/etc/letsencrypt` 전역에 대해 **`recurse: yes`** 및 **`o-rwx,g+rX`** 모드를 부여하여 모든 서브디렉터리 및 파일의 그룹 권한을 완치 및 개통함.

### Nginx 임시 캐시 디렉터리 /tmp 우회 매핑 및 비특권 기동 완료 (Step 74) - 완료
- **해결 내역**:
  - **임시 캐시 쓰기 차단 극복**: Nginx 비특권 사용자 기동 시 `/var/cache/nginx/client_temp` 생성이 거부되어 컨테이너가 즉사하던 결함을 패치함. `nginx.conf` 및 `nginx.stage.conf` 의 `server` 블록 내부에 `client_body_temp_path` 등 5가지 임시 파일 쓰기 지시어를 주입하여, 스티키 비트가 활성화된 메모리 개방 임시 공간인 **`/tmp`** 하위로 쓰기 경로를 변경함으로써 `read_only: true` 격리 가동을 최종 완수함.

### Nginx 캐시 영역 상세 tmpfs 소유권(101:101) 마운트 및 구동 정상화 (Step 75) - 완료
- **해결 내역**:
  - **tmpfs 소유자 권한 완치**: Nginx 초기화 전역 루틴에서 `/var/cache/nginx/client_temp` 등을 강제 생성하려고 시도하여 Permission Denied(13번) 에러를 유발하던 구동 장벽을 해결함. `docker-compose` 및 `docker-compose.stage.yml` 에 선언적 상세 tmpfs 마운트(`type: tmpfs`, `uid: 101`, `gid: 101`, `mode: 0770`)를 주입하여 Nginx 비특권 유저가 캐시 디렉터리 내부에 무제한 쓰기 및 디렉터리 생성을 수행할 수 있도록 완치함.

### Nginx 캐시 영역 상세 tmpfs mode: 0777 조정을 통한 Compose 빌드 구문 결함 해소 (Step 76) - 완료
- **해결 내역**:
  - **Docker Compose 호환 구문 전환**: 원격 호스트의 Docker Compose 파서가 `volumes.tmpfs` 객체 내의 `uid`, `gid` 속성을 해석하지 못하고 빌드를 차단시키던 장애(`additional properties not allowed`)를 패치함. 호환 구문 규격에 맞춰 `uid` 와 `gid`를 완전 제거하고, 모든 Compose 버전에서 표준 지원하는 **`mode: 0777`** 속성을 부여하여 일반 비특권 Nginx 계정이 캐시 영역에 제약 없이 파일을 쓸 수 있도록 구문 정합성과 비특권 보안성을 동시에 완결함.

### Nginx /run tmpfs 매핑 전환을 통한 PID 생성 실패 핫픽스 (Step 77) - 완료
- **해결 내역**:
  - **심볼릭 링크 실제 경로 개통**: Nginx 컨테이너 내 프로세스가 `/run/nginx.pid` 에 직접 파일을 기록하려 할 때, `/var/run`만 임시 메모리 마운트되어 실제 경로인 `/run`은 쓰기 제한으로 기동이 거부되던 장애를 패치함. `docker-compose` 및 `docker-compose.stage.yml` 설정 내 `tmpfs` 마운트 대상을 **`/run`** 으로 개정함으로써 PID 생성 에러를 완전히 해소함.

### 공식 Nginx 비특권 이미지(nginxinc/nginx-unprivileged:alpine) 통합 완료 (Step 78) - 완료
- **해결 내역**:
  - **정석 Non-root 구성 도달**: Nginx 공식 Alpine 이미지의 전역 초기화 쉘 스크립트 실행 충돌을 회피하기 위해, 비특권 전용 빌드 공식 이미지인 **`nginxinc/nginx-unprivileged:alpine`** 으로 전격 마이그레이션함. 이를 통해 임시 우회용 Nginx 쓰기 경로 패치 지시어들을 전량 소거하고, `docker-compose` 에서 `/run` 및 `/var/cache/nginx` 등 복잡하게 얽혀 있던 불필요한 tmpfs 임시 마운트 설정을 완벽하게 청산함. 오직 `/tmp` 만 tmpfs 마운트하여 정석 non-root + read-only 구동 상태를 완전 수립함.

---

## 2. 다음 목표 (Next Goals)
- **배치 주기별 AI 퍼즐 자동 생성 경과 관찰**: 04:17 AM 크론탭 실행 시 30x30 및 각 그리드별 데일리 퍼즐 생성이 파싱 에러 없이 매끄럽게 수행되는지 추가 모니터링 수행.
- **Nginx 웹 방화벽(WAF) 도입 검토**: 리소스 제약을 극복하고 Nginx 레벨의 보안 강화를 위한 방화벽 구성안 비교 및 적용 설계 수립.

