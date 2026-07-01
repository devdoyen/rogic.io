# rogic.io (Rotate Logic Nonogram Puzzle)

[![CI/CD Pipeline Status](https://github.com/devdoyen/rogic.io/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/devdoyen/rogic.io/actions/workflows/ci-cd.yml)

🚀 **Live Services**:
- **Production Environment**: [rogic.io](https://rogic.io)
- **Staging Environment**: [stage.rogic.io](https://stage.rogic.io)

`rogic.io`는 3차원 서브 그리드 회전 역학을 도입한 차세대 노노그램 논리 퍼즐 플랫폼입니다. 플레이어는 그리드의 특정 섹션을 회전하고 패턴을 맞추어 숨겨진 그림을 찾아냅니다.

본 저장소는 자동화된 CI/CD 파이프라인, 선언적 IaC 클라우드 프로비저닝, 구성 관리 자동화, 그리고 애플리케이션 가용성과 보안성을 극대화한 실무 수준의 인프라 아키텍처 포트폴리오를 포함하고 있습니다.

---

## 🛠 Technology Stack

| Category | Technologies | Description |
| :--- | :--- | :--- |
| **Frontend** | `Vue 3`, `TypeScript`, `HTML5 Canvas API`, `Axios` | Client app with decoupled pure TS game engine. |
| **Backend** | `Java 17`, `Spring Boot`, `Spring Data JPA` | REST API layer for stage state, history, and users. |
| **Database** | `PostgreSQL 16` | Relational storage for user logs, clear history, and stages. |
| **Infra & IaC** | `AWS`, `Terraform`, `Ansible`, `Docker Compose` | Code-defined AWS resources & automated config deployment. |
| **CI/CD** | `GitHub Actions`, `Vitest`, `Playwright` | Path-filtered tests, browser E2E validation, auto-SemVer. |
| **Telemetry** | `Prometheus`, `Grafana Cloud`, `CloudWatch` | Agentless scraping, log alarms, SNS email alerting. |

---

## 📐 System Architecture

네모로직 서비스는 극단적인 비용 최적화(t3a.nano/t4g.nano)를 유지하면서도 가용성과 복구 속도를 보장하도록 인프라가 설계되어 있습니다.

```mermaid
graph TD
    subgraph "AWS Cloud (ap-northeast-2)"
        subgraph "VPC (10.0.0.0/16)"
            subgraph "Public Subnet (10.0.1.0/24)"
                EIP[Elastic IP] <--> EC2[EC2 Instance: t3a.nano]
                subgraph "Docker Compose Environment"
                    Nginx[Nginx Container: Port 80/443] <--> Spring[Spring Boot Container: Port 8080]
                    Spring <--> Postgres[(PostgreSQL Container: Port 5432)]
                end
                NodeExporter[Node Exporter: Port 9100]
                Nginx <-->|Proxy metrics| NodeExporter
            end
        end
        
        EC2 -->|Log Driver: awslogs| CWL[CloudWatch Log Group]
        CWL -->|Metric Filter| CWA[CloudWatch Alarm]
        CWA -->|Trigger| SNS[SNS Topic]
        SNS -->|Email Subscription| DevEmail([개발자 수신함])
        
        EC2 -->|6-Hour cron| S3B[S3 Backup Bucket]
        TFS[(DynamoDB & S3)] <-->|State Locking| TF[Terraform Engine]
    end
    
    GC[(Grafana Cloud Prometheus/Mimir)] -->|Agentless Pull: Bearer Auth| Nginx
    GC_CW[(Grafana Cloud CloudWatch DS)] -->|Pull logs| CWL
```

* **Frontend Hosting & Multi-Origin CDN**: Static HTML/JS bundle compiled via Vite, hosted on `Amazon S3` (private access via OAC), and distributed globally through `Amazon CloudFront` CDN.
* **Backend API & E2E HTTPS**: EC2 API server runs Spring Boot as Docker container with Nginx reverse proxy. A dedicated backend domain (`api.rogic.io` / `api.stage.rogic.io`) is mapped directly to the EC2 Elastic IP. CloudFront acts as a unified entry point, routing `/api/*` and `/actuator/*` over E2E HTTPS to the backend, with caching disabled for API paths.
* **Telemetry**: Prometheus actuator endpoints are exposed securely via Nginx token authentication (`Authorization: Bearer`), allowing agentless metric scraping directly by Grafana Cloud.

<details>
<summary>🔍 Click to view Inframap Generated Resource Dependency Graphs</summary>

#### Staging Environment Infrastructure Graph
![Staging Infrastructure Graph](./docs/images/staging-infra.png)

#### Production Environment Infrastructure Graph
![Production Infrastructure Graph](./docs/images/production-infra.png)

</details>

---

## 🚀 CI/CD & GitOps Pipeline

본 저장소는 브랜치 푸시부터 실서버 릴리즈까지 전체 과정을 GitHub Actions와 IaC 코드로 제어하는 GitOps 배포 파이프라인을 구축했습니다.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> CI : Git Push to main
    
    state "1. Continuous Integration (CI)" as CI {
        direction TB
        state "Backend: Gradle Tests" as UnitB
        state "Frontend: Vitest Tests" as UnitF
        state "Infra: Ansible Lint" as Lint
        
        [*] --> UnitB
        [*] --> UnitF
        [*] --> Lint
    }

    state "2. Continuous Delivery: Staging" as Staging {
        direction TB
        state "Build Backend (GHCR)" as BuildB
        state "Build & S3 Sync Frontend" as BuildF
        state "Terraform Apply Staging" as TFA_S
        state "Deploy Backend via Ansible" as Deploy_S
        state "Run Playwright E2E Tests" as E2E

        [*] --> BuildB
        [*] --> BuildF
        BuildB --> TFA_S
        BuildF --> TFA_S
        TFA_S --> Deploy_S
        Deploy_S --> E2E
    }

    state "3. Approval Gate" as Gate {
        state "Pause for Admin Manual Approval" as Approve
        [*] --> Approve
    }

    state "4. Continuous Deployment: Production" as Production {
        direction TB
        state "Terraform Apply Production" as TFA_P
        state "Deploy Production via Ansible" as Deploy_P
        state "Auto-SemVer Tag & Release" as Release

        [*] --> TFA_P
        TFA_P --> Deploy_P
        Deploy_P --> Release
    }

    CI --> Staging : Validations Pass
    Staging --> Gate : Playwright E2E Pass
    Gate --> Production : Approved
    Production --> [*] : Production Release Complete
```

### Key Workflow Characteristics:
1. **Path-Filtered Execution**: GitHub Actions가 변경된 파일의 디렉토리를 평가하여 인프라 설정 변경 시에는 빌드/컴파일 단계를 우회함으로써 대기 시간을 최소화합니다.
2. **Playwright E2E Gating**: Staging 배포 완료 즉시 Playwright 헤드리스 브라우저가 실제 로그인, 퍼즐 보드 로드, 기록 저장 등의 사용자 흐름을 E2E 검증합니다.
3. **Manual Approval Gate**: 운영 환경(Production) 배포 시 관리자의 명시적 승인이 있어야만 Terraform Apply 및 Ansible 배포가 트리거되도록 안전 장치를 마련했습니다.
4. **Auto-SemVer Tagging & Release**: 운영 배포 완료 시 커밋 메시지 규칙(`feat:`, `fix:`)을 파싱하여 SemVer 태그를 자동으로 생성하고 GitHub Release 노트를 발행합니다.
5. **격리된 VPC 배포 및 동시성 제어**: Staging 인프라는 완전히 격리된 별도의 사설 가상 네트워크망(VPC `10.1.0.0/16`)에 승인 없이 자동 배포되며, 배포 동시성 제어(`cancel-in-progress: true`)를 도입해 신규 커밋 푸시 즉시 기존 중복 승인 대기 파이프라인을 자동 소거합니다.

---

## ⚙️ 핵심 인프라 구현 상세 (Core DevOps Implementations)

### 1. 선언적 클라우드 프로비저닝 (Terraform)
* **VPC 및 네트워크 설계**: Production VPC(`10.0.0.0/16`, 서브넷 `10.0.1.0/24`) 및 Staging VPC(`10.1.0.0/16`, 서브넷 `10.1.1.0/24`)를 물리적으로 완벽히 격리하여 라우팅 제어.
* **보안 그룹 최소 권한 권장**: SSH(22), Nginx HTTP(80), HTTPS(443), Spring Boot HTTP(8080) 포트 인입만 허용하고 아웃바운드는 전면 오픈.
* **형상 잠금 및 원격 보존**: S3 버킷과 DynamoDB 테이블(`LockID` 해시 키)을 백엔드로 연동하여 다중 개발 환경에서의 동시 배포 시 발생하는 State 충돌을 원천 차단.
* **EC2 Auto Recovery**: 시스템 상태 검사 실패 발생 시 자동으로 동일 EIP/EBS 설정을 유지한 채 건강한 물리 서버로 가상머신이 자동 복구되도록 CloudWatch Metric Alarm 연동.

### 2. 서버 구성 자동화 및 컨테이너 오케스트레이션 (Ansible & Docker)
* **GitHub Actions & GHCR 빌드 오프로딩**:
  * **백엔드**: 512MB RAM 환경에서의 AOT 컴파일 병목을 방지하기 위해 빌드 과정을 GitHub Actions Runner로 이관(Offloading)하고 운영 서버에서는 미리 완성된 도커 이미지를 Pull 받아 컨테이너만 기동.
  * **프론트엔드**: S3 업로드(`aws s3 sync`) 및 CDN 캐시 무효화(`aws cloudfront create-invalidation`) 단계를 배포 워크플로우에 적용하여 서버 빌드 부하 전면 제거.
* **Docker Compose 통합 스택 배포**: `db`, `backend`, `frontend` 서비스를 단일 가상 네트워크로 결합하고 `depends_on` 헬스체크 제어를 통해 종속성 보장.
* **Ansible Playbook 기반 멱등성 구성**: 스왑 메모리(1.5GB) 구성, 필수 패키지 설치, Docker 엔진 주입, 소스 배포까지 전 과정을 자동화하여 멱등성을 지닌 셋업 지원.
* **S3 DB 백업 및 주기 고도화**: 매 6시간 주기(00, 06, 12, 18시)로 PostgreSQL 덤프 압축본을 S3 버킷에 자동 소산시키는 cron 작업 및 S3 버킷 내 30일 경과 백업 데이터 자동 삭제 수명 주기 정책 수립.

### 3. Let's Encrypt 및 Nginx HTTPS 보안
* **도메인 및 SSL 인증서 연동**: Route53 A 레코드를 고정 탄력적 IP(EIP)와 매핑하고 Certbot을 통해 Let's Encrypt SSL 인증서 발급.
* **SSL/TLS 종단 처리 (SSL Termination)**: Nginx 내부로 인증서 경로를 마운트하고 443 포트와 SSL 프로토콜 설정을 바인딩하여 안전한 HTTPS 통신 구현.
* **HTTP to HTTPS 자동 리다이렉트**: 포트 80으로 유입되는 평문 HTTP 요청을 HTTPS(443)로 강제 자동 전환.
* **자동 갱신 데몬 연동**: Nginx 컨테이너 중지, Standalone 검증 실행, 컨테이너 재가동 과정을 Pre/Post Hooks 스크립트로 3개월마다 실행되도록 자동 연동.

### 4. Observability & Agentless Pull Prometheus
* **Docker awslogs 드라이버 연동**: 수집 에이전트의 서버 점유율을 0%로 통제하기 위해 컨테이너 표준 출력을 CloudWatch Logs `/aws/ec2/nemologic`로 직접 offload.
* **CloudWatch Log Metric Filter & SNS**: `?ERROR ?" 500 "` 등의 시스템 장애 발생 시 메트릭 경보를 작동시켜 AWS SNS 이메일로 알림 전송.
* **Agentless Pull 메트릭 수집**: Grafana Alloy 에이전트를 배포에서 제외하고, 10MB 미만의 Node Exporter와 Spring Boot Actuator `/actuator/prometheus`를 Nginx 프록시를 통해 안전하게 개방.
* **메트릭 프록시 보안 및 격리**: 외부 Prometheus 수집 시 Bearer Token 인증 헤더(`Authorization: Bearer nemologic-metrics-token-2026`)를 Nginx에서 검증하여 비인가된 수집을 차단하고, OS 메트릭에 환경 분리 필터(`$env`)를 적용해 staged 데이터 수집을 격리.
* **Synthetic Monitoring & SLA 대시보드 (IaC)**:
  * 전 세계 3개 리전(도쿄, 싱가포르, 시드니) Probes가 `/actuator/health` 엔드포인트를 60초 주기로 검사하도록 설정.
  * 3개 Probes 동시 실패 시 개발자 이메일 연락처로 장애 경보 규칙(`Nemologic-Service-Down-Alert`)이 작동하도록 Grafana Provider를 통해 코드로 정의.
  * JVM 메트릭, SLA 지표(실시간 Uptime, 24h/7d/30d 기간별 가용성, Incidents 수, MTTR, MTBF) 및 OS 자원 지표, 애플리케이션 로그 뷰어를 단일 화면으로 최적화한 통합 대시보드 스키마(`current_dashboard.json`)에 병합해 관리하도록 Terraform 리소스로 자동화.
  * **대시보드 레이아웃 확인용 퍼블릭 링크**: [Grafana Live Public Dashboard](https://grandwalrus3189.grafana.net/public-dashboards/ec9e06b0d1ea4540b97af6b56abb1380) 링크를 통해 구축된 모니터링 시스템의 시각화 레이아웃 및 차트 배치 구조를 외부에서도 직접 확인해 볼 수 있습니다. (보안 정책 상 실제 메트릭 데이터 대신 구조 확인용 임의 지표가 노출됩니다.)

#### [부록] SLA 및 신뢰성 분석을 위한 PromQL 수식 정의
실시간 가동률 분석 및 복구 품질 정량 측정을 위해 통합 대시보드 최상단 행(Nemologic Service SLA Metrics)에 탑재된 핵심 PromQL 공식입니다.
* **실시간 가동 여부 (API Health Status)**: `sum(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"})`
* **30일 평균 가용성 가동률 (30-Day Service Availability)**: `avg_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) * 100`
* **30일 누적 장애 발생 건수 (30-Day Incident Count)**: `changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2`
* **평균 복구 시간 (MTTR, Mean Time To Recovery)**: `((count_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) - sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d])) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2, 1)`
* **평균 고장 간격 (MTBF, Mean Time Between Failures)**: `(sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2, 1)`

---

## 📈 서비스 신뢰성 및 재해 복구 지표 (SLA & DR Metrics)

본 아키텍처의 가용성 수준과 장애 복구 능력을 정량 평가하기 위해 설정한 목표 대비 현재 구현 사양입니다.

| 지표 | 현재 환경 (단일 EC2 + S3 백업) | 향후 개선 목표 (Multi-AZ ALB + ECS/RDS) |
| :--- | :--- | :--- |
| **RPO (복구 시점)** | **6시간** (하루 4회 S3 백업 소산) | **5분 이내** (RDS Multi-AZ 및 PITR 자동 활성화) |
| **RTO (복구 시간)** | **약 20분** (Terraform 프로비저닝 복구 및 DB 덤프 복원) | **1분 이내** (ALB 액티브 백업 및 컨테이너 무중단 교체) |
| **MTBF (평균 고장 간격)** | **낮음** (t3a.nano 노드 리소스 병목 리스크 존재) | **매우 높음** (컴퓨팅 자원 분리 및 2GB 이상 스케일링) |
| **MTTR (평균 복구 시간)** | **약 10분** (경보 감지 후 관리자의 수동 개입 및 재부팅) | **10초 이내** (ALB 헬스체크 및 Fargate Self-healing 자동 복구) |

---

## 🧠 AI 엔지니어링 및 어시스턴트 활용 (AI Integration)

본 서비스는 Google Gemini API를 이용한 자동 퍼즐 생성 파이프라인 및 AI 페어 프로그래밍을 적극 채택했습니다.

### 1. Gemini API 기반 무한 데일리 퍼즐 생성기
* **실시간 생성 스케줄러**: 넉넉한 일일 할당량(500 RPD)을 가진 `gemini-3.1-flash-lite` 모델을 스프링 스케줄러에 연동하여 일시적 API 장애 예방을 위한 최대 3회 자동 재시도 루프 및 5초 지연 시간 설계.
* **배치 검증 및 정제**: 1회 호출로 5개의 서로 다른 퍼즐 후보군을 JSON Array로 수집하는 프롬프트를 설계하고, 생성된 퍼즐 타이틀의 무작위 접두사를 정규식 기반으로 클리닝 처리.
* **비정형 분리형 릴리즈 패턴**: 새벽 04:17에 비활성 상태(`active = false`, `approved = true` 자동승인)로 퍼즐을 백그라운드 생성한 뒤, 매일 자정 00:00에 배치를 실행하여 최종 유저에게 릴리즈 노출.

### 2. Java 기반 노노그램 솔버를 통한 100% 논리해 검증
* **100% 논리해 (Logical-only) 검증**: 찍어 맞추는 과정(Backtracking) 없이 순수 논리적 추론으로만 해결 가능한지 판별하는 `isLogicalOnly(grid)` 검사 필터를 설계하여 유일해 및 최상 후보를 선별하는 2단계 검증 필터 적용. 5회 전체 후보 실패 시 명시적인 예외를 던져 데이터 신뢰성 100% 보장.
* **30x30 고속 최적화**: 대형 퍼즐 탐색 시 발생하는 OOM 및 타임아웃을 차단하기 위해 동적 계획법(DP) 기반 행/열 해결기(`solveLine`) 및 미결정 셀 대상 부분 DFS 탐색 구조를 결합해 검증 연산 시간을 1ms 이하로 단축.
* **사용자 피드백 시스템 통합**: 퍼즐 클리어 시 👍 / 👎 평가 위젯(Glassmorphism 다크 테마 및 커스텀 SVG 아웃라인 아이콘)을 제공하고, 백오피스 어드민 대시보드에 피드백을 실시간 집계하여 만족도가 낮은 퍼즐을 즉시 삭제 가능하도록 조치.

---

## 💡 비용 최적화 설계 및 기술적 타협 (Cost Optimization & Trade-offs)

1. **무상태(Stateless) 인프라 설계를 통한 고가용성 타협 (ALB 배제)**
   * **비용 절감**: AWS ALB 고정 비용(월 약 $20)을 절감하기 위해 Route 53과 단일 EC2 인스턴스 구조로 아키텍처 단순화.
   * **보완 대책**: 인스턴스 장애 시 CloudWatch 경보와 연동한 자동 재기동을 설정하고, 재해 시 IaC 코드를 활용하여 5분 이내에 인프라 전체를 동일하게 복구하는 복구 지향형 아키텍처 구현.
   * **Active-Active 무중단 배포**: GraalVM Native Image 도입을 통해 메모리를 30MB 수준으로 절감하여, 배포 완료 후에도 Blue/Green 백엔드가 모두 가동되는 Active-Active 구조를 유지. 배포 시점에만 임시로 모니터링 수집을 정지하여 OOM을 원천 방어.
2. **Self-Hosted 데이터베이스 및 S3 백업 파이프라인 (RDS 대체)**
   * **비용 절감**: AWS RDS 구동 비용(월 약 $15~20)을 제거하기 위해 단일 EC2 내 PostgreSQL 도커 컨테이너 기동.
   * **보완 대책**: 매 6시간마다 DB 덤프 압축본을 S3 버킷으로 소산시키는 자동화 스크립트를 작성하고 S3 버킷 라이프사이클을 통해 30일 경과 파일 자동 폐기를 구성하여 데이터 유실 안전망과 비용 최소화 달성.
3. **자원 제약 환경에 따른 애플리케이션 및 모니터링 최적화**
   * **비용 절감**: 장기 운영 비용 최소화를 위해 극저비용 컴퓨팅 노드인 `t3.nano` / `t4g.nano` (512MB RAM) 환경 영구 선택.
   * **AOT 컴파일 및 Reflection 힌트**: Spring Boot 애플리케이션에 GraalVM AOT 컴파일을 적용해 런타임 메모리 사용량을 30MB 안팎으로 경량화. AOT 환경에서 리소스 탐색 오류(`FileNotFoundException`)를 방지하기 위해 [stages.json](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/puzzles/stages.json) 리소스를 명시적 직접 조회 방식으로 설계하고, static DTO 런타임 힌트([NemologicRuntimeHints.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java))를 명시적으로 등록.
   * **Agentless Pull 전환**: 512MB RAM의 극심한 자원 제약 하에서 무거운 에이전트(Grafana Alloy) 대신 10MB 미만의 Node Exporter와 Nginx, Bearer Token 검증을 결합한 Agentless Pull 수집 아키텍처로 오버헤드를 사실상 0으로 최소화.

---

## 🔮 누락된 구성 요소 및 인프라 개선 과제 (Roadmaps)

실무급 프로덕션 환경으로 확장하기 위해 향후 보완되어야 할 사항 및 마이그레이션 권장 요소입니다.

1. **네트워크 다중 보안 격리 (Private Subnet 전환)**: DB와 EC2를 Private Subnet으로 내리고, 외부의 요청은 퍼블릭 서브넷의 ALB 및 Bastion Host/SSM Session Manager를 통해서만 유입되도록 격리.
2. **단일 장애점(SPOF) 제거**: EC2 인스턴스를 Auto Scaling Group(ASG)으로 묶고, 도커 구성을 AWS의 컨테이너 관리 서비스인 **ECS (Fargate)** 또는 **EKS (Kubernetes)**로 마이그레이션하여 자동 복구 루프 구성.
3. **비밀번호 및 API 키 관리 체계 강화**: `.env` 텍스트 보관 대신 **AWS Secrets Manager**와 연동하여 IAM Role 권한 기반 동적 Dynamic Fetch 방식으로 전환.
4. **DevSecOps 및 자동화된 보안 취약성 검사**: CI/CD 파이프라인 빌드 시 **CodeQL / SonarQube** (SAST 소스 진단), **Snyk / OWASP Dependency-Check** (SCA 라이브러리 검사), **Trivy** (컨테이너 이미지 취약점 스캔)를 연동한 Quality Gate 구성.

---

## 💻 Local Development Setup

To run `rogic.io` on your local workstation:

### Prerequisites
* Java 17 JDK
* Node.js 20+
* Docker & Docker Compose

### Step 1: Start PostgreSQL Database
```bash
# In project root
docker compose -f docker-compose.local.yml up -d db
```

### Step 2: Run Backend API
```bash
cd backend
./gradlew bootRun
```
* API Server will run on: `http://localhost:8080`

### Step 3: Run Frontend Client
```bash
cd frontend
npm install
npm run dev
```
* Frontend app will run on: `http://localhost:5173`
