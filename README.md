# rogic.io (Rotate Logic Nonogram Puzzle)

[![CI/CD Pipeline Status](https://github.com/devdoyen/rogic.io/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/devdoyen/rogic.io/actions/workflows/ci-cd.yml)

🚀 **Live Services**:
- **Production Environment**: [rogic.io](https://rogic.io)
- **Staging Environment**: [stage.rogic.io](https://stage.rogic.io)

`rogic.io`는 3차원 서브 그리드 회전 역학을 도입한 차세대 노노그램 논리 퍼즐 플랫폼입니다. 플레이어는 그리드의 특정 섹션을 회전하고 패턴을 맞추어 숨겨진 그림을 찾아냅니다.

본 저장소는 자동화된 CI/CD 파이프라인, 선언적 IaC 클라우드 프로비저닝, 구성 관리 자동화, 그리고 애플리케이션 가용성과 보안성을 극대화한 실무 수준의 인프라 아키텍처 포트폴리오를 포함하고 있습니다.

---

## 📋 Table of Contents
- [🛠 Technology Stack](#-technology-stack)
- [1. Infrastructure & Cloud Engineering](#1-infrastructure--cloud-engineering)
  - [1-1. System Architecture](#1-1-system-architecture)
  - [1-2. Cost Optimization & Technical Trade-offs](#1-2-cost-optimization--technical-trade-offs)
  - [1-3. Network & Security Architecture](#1-3-network--security-architecture)
  - [1-4. Observability & SRE (Site Reliability Engineering)](#1-4-observability--sre-site-reliability-engineering)
- [2. Continuous Integration & Delivery (CI/CD)](#2-continuous-integration--delivery-cicd)
  - [2-1. Pipeline Workflow](#2-1-pipeline-workflow)
  - [2-2. Build & Artifact Management](#2-2-build--artifact-management)
  - [2-3. Quality Gate & Release Automation](#2-3-quality-gate--release-automation)
- [3. AI Engineering & Intelligent Systems](#3-ai-engineering--intelligent-systems)
  - [3-1. AI Puzzle Generator & Logical Validation Pipeline](#3-1-ai-puzzle-generator--logical-validation-pipeline)
  - [3-2. User Feedback & Backoffice Monitoring System](#3-2-user-feedback--backoffice-monitoring-system)
  - [3-3. AI Agentic Development & Governance (.agents/rules)](#3-3-ai-agentic-development--governance-agentsrules)
- [💻 Local Development Setup](#-local-development-setup)

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

## 1. Infrastructure & Cloud Engineering

### 1-1. System Architecture
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

* **Frontend Hosting & Multi-Origin CDN**: Vite로 컴파일된 정적 HTML/JS 번들을 Amazon S3 버킷(OAC를 통해 전면 보안 폐쇄)에 호스팅하고, Amazon CloudFront CDN을 통해 전 세계에 최적 분포 전송합니다.
* **Backend API & E2E HTTPS**: 단일 Amazon EC2 호스트 내에서 Spring Boot 앱이 도커 컨테이너로 가동되며, Nginx가 리버스 프록시 및 SSL/TLS 종단점으로 전면 매핑됩니다. API 및 Actuator 경로는 전용 백엔드 도메인(`api.rogic.io` / `api.stage.rogic.io`) 및 CloudFront Multi-Origin 규칙을 통해 암호화(E2E HTTPS) 통신을 전 구간 보장하며 API 호출에 대한 CDN 캐싱은 전면 비활성화되어 있습니다.
* **Telemetry**: 수집 데몬(Grafana Alloy 등)을 EC2 호스트 내에서 영구 배제하여 자원 소모를 0%로 만들고, Prometheus Actuator 경로(`/actuator/prometheus`)만 Nginx token 인증 헤더 검증(`Authorization: Bearer nemologic-metrics-token-2026`)을 기반으로 안전하게 개방해 외부 Grafana Cloud가 직접 Scrape(Agentless Pull)하도록 설계했습니다.

<details>
<summary>🔍 Click to view Inframap Generated Resource Dependency Graphs</summary>

#### Staging Environment Infrastructure Graph
![Staging Infrastructure Graph](./docs/images/staging-infra.png)

#### Production Environment Infrastructure Graph
![Production Infrastructure Graph](./docs/images/production-infra.png)

</details>

### 1-2. Cost Optimization & Technical Trade-offs
개인 사비로 운영되는 환경 특성상 상시 결제 비용을 최저로 통제하면서도 상용 수준의 가용성을 유지하기 위한 핵심 인프라 타협 설계입니다.

1. **ALB(Load Balancer) 배제를 통한 무상태(Stateless) 인프라 구성**
   * **비용 절감**: AWS ALB 고정 비용(월 약 $20)을 절감하기 위해 Route 53 DNS와 고정 탄력적 IP(EIP) 단일 EC2 구조로 아키텍처를 단순화했습니다.
   * **대체 수단**: 서버 장애 시 CloudWatch Metric Alarm과 연동해 EC2 물리 호스트 수준에서 자동으로 마이그레이션 기동을 트리거하는 **EC2 Auto Recovery**를 적용하고, 재해 발생 시 Terraform 및 Ansible 코드를 기반으로 5분 이내에 인프라와 배포를 똑같이 복구할 수 있는 **복구 지향형 아키텍처(Recovery-Oriented Architecture)**를 채택했습니다.
   * **Active-Active 무중단 배포**: GraalVM Native Image 컴파일을 통해 런타임 메모리 소모를 30MB 안팎으로 극단적으로 경량화함으로써, 단일 VM 환경에서도 Blue와 Green 컨테이너가 배포 완료 후에도 모두 상시 가동되는 Active-Active 구조를 영구 유지하며 롤백 가용성을 보장합니다.
2. **PostgreSQL 컨테이너 및 S3 백업 파이프라인 (RDS 대체)**
   * **비용 절감**: AWS RDS 구동 비용(월 약 $15~20)을 완전히 절감하기 위해 단일 EC2 인스턴스 내 Docker Compose 기반 PostgreSQL 컨테이너를 구동합니다.
   * **대체 수단**: 매 6시간마다(00, 06, 12, 18시) DB 덤프 파일을 압축해 S3 백업 버킷으로 안전하게 이중화 전송하는 쉘 스크립트와 크론탭(Cron Job)을 구성했으며, 백업 데이터 누적으로 인한 비용 방지를 위해 S3 버킷에 30일 경과 백업 데이터 자동 영구 삭제 수명 주기(Lifecycle Rules) 정책을 적용했습니다.
3. **자원 제약 환경에 따른 애플리케이션 및 모니터링 최적화**
   * **비용 절감**: 월 $3.5 수준의 극단적 저비용 컴퓨팅 노드인 `t3.nano` / `t4g.nano` (512MB RAM) 환경을 타겟으로 고정했습니다.
   * **대체 수단**:
     * **AOT 컴파일 및 Reflection 힌트**: Spring Boot 애플리케이션에 GraalVM AOT 컴파일을 적용해 메모리 점유율을 30MB 이하로 낮췄습니다. Native Image의 클래스패스 리소스 리팩토링으로 AOT 컴파일 타임 리소스 스캔 오류(`FileNotFoundException`)를 원천 차단하고, Jackson 역직렬화 DTO 클래스들의 Reflection 런타임 힌트([NemologicRuntimeHints.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java))를 명시적으로 등록해 실행 안정성을 다졌습니다.
     * **Docker Garbage Collection 자동화**: 디스크 고갈로 인한 장애를 차단하기 위해 매일 새벽 3시마다 72시간 동안 미사용된 컨테이너 레이어, 볼륨, 이미지 캐시를 안전하게 소거하는 `docker system prune -af --volumes` 스케줄 크론탭을 Ansible 플레이북으로 자동 배포했습니다.

### 1-3. Network & Security Architecture
* **VPC 사설망 격리 및 네트워크 차단**: Staging VPC(`10.1.0.0/16`, 서브넷 `10.1.1.0/24`)와 Production VPC(`10.0.0.0/16`, 서브넷 `10.0.1.0/24`)를 독립적인 VPC망으로 완전 분리 프로비저닝하여 네트워크를 물리적으로 격리했습니다.
* **보안 그룹 최소 권한 권장**: SSH(22), Nginx HTTP(80), HTTPS(443), Spring Boot HTTP(8080) 포트 인입만 보안 그룹을 통해 수용하고 아웃바운드는 전면 오픈했습니다.
* **ACM 및 Let's Encrypt SSL/TLS**: Route53 도메인 A 레코드 고정 바인딩 후, Nginx 컨테이너 내부로 SSL 인증서 경로를 마운트하고 HTTPS(443) 종단 처리 및 HTTP(80) 301 강제 리다이렉트 보안 구성을 완료했습니다. 3개월 주기 자동 갱신을 위해 Nginx 기동을 연동하는 pre/post hooks 쉘 스크립트를 Certbot에 통합 구성했습니다.
* **형상 잠금 (State Locking)**: S3 버킷과 DynamoDB 테이블(`LockID` 해시 키)을 테라폼 원격 백엔드로 결합하여 다중 배포 환경에서의 동시 수정 시 발생하는 형상(State) 깨짐 및 충돌을 차단했습니다.

### 1-4. Observability & SRE (Site Reliability Engineering)
* **Agentless Pull 메트릭 수집 및 Nginx 프록시 보안**: 에이전트 소모 메모리 조차 용납하지 않는 극단적 저사양 VM 환경을 위해 수집 에이전트를 제거했습니다. 대신 Nginx 리버스 프록시 레벨에서 외부 Prometheus 스크래퍼가 유입될 때 `Authorization: Bearer` 헤더 토큰을 정교하게 대조 검증(401 Unauthorized 차단)하는 수집 전용 가상 경로를 개방하여 보안성과 모니터링 효율성을 결합했습니다.
* **Docker awslogs 드라이버 연동**: 컨테이너 표준 출력을 AWS CloudWatch Logs `/aws/ec2/nemologic` 로그 그룹으로 직접 Offload하여 로컬 디스크 및 메모리 점유율을 0%로 만들고 스팸성 스크래핑 로그 수집을 원천 차단했습니다.
* **Synthetic Monitoring & SLA 대시보드 (IaC)**:
  * 전 세계 3개 리전(도쿄, 싱가포르, 시드니) Probes가 헬스 엔드포인트 `/actuator/health`를 60초 간격으로 검증하도록 Grafana 코드로 선언 배포했습니다.
  * 3개 프로브 동시 실패 감지 시 메일로 실시간 장애 알림을 발송하는 경보 규칙(`Nemologic-Service-Down-Alert`)을 SNS 토픽과 바인딩했습니다.
  * 대시보드 리소스 스키마([current_dashboard.json](file:///c:/Users/82107/dev/project/nemologic/infra/monitoring/current_dashboard.json))에 SRE 관제의 핵심인 기간별 SLA 지표(Incident Count, Uptime SLA, MTTR, MTBF)를 복구 탑재하여 3열 카드형 레이아웃 형태로 배치 관리하고 있습니다.
  * **대시보드 레이아웃 확인용 퍼블릭 링크**: [Grafana Live Public Dashboard](https://grandwalrus3189.grafana.net/public-dashboards/ec9e06b0d1ea4540b97af6b56abb1380) 링크를 통해 구축된 모니터링 시스템의 시각화 레이아웃 및 차트 배치 구조를 외부에서도 직접 확인해 볼 수 있습니다. (보안 정책 상 실제 메트릭 데이터 대신 구조 확인용 임의 지표가 노출됩니다.)

#### [부록] SLA 및 신뢰성 분석을 위한 PromQL 수식 정의
* **실시간 가동 여부 (API Health Status)**: `sum(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"})`
* **30일 평균 가용성 가동률 (30-Day Service Availability)**: `avg_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) * 100`
* **30일 누적 장애 발생 건수 (30-Day Incident Count)**: `changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2`
* **평균 복구 시간 (MTTR, Mean Time To Recovery)**: `((count_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) - sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d])) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2, 1)`
* **평균 고장 간격 (MTBF, Mean Time Between Failures)**: `(sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[30d]) / 2, 1)`

| 지표 | 현재 환경 (단일 EC2 + S3 백업) | 향후 개선 목표 (Multi-AZ ALB + ECS/RDS) |
| :--- | :--- | :--- |
| **RPO (복구 시점)** | **6시간** (하루 4회 S3 백업 소산) | **5분 이내** (RDS Multi-AZ 및 PITR 자동 활성화) |
| **RTO (복구 시간)** | **약 20분** (Terraform 프로비저닝 복구 및 DB 덤프 복원) | **1분 이내** (ALB 액티브 백업 및 컨테이너 무중단 교체) |
| **MTBF (평균 고장 간격)** | **낮음** (t3a.nano 노드 리소스 병목 리스크 존재) | **매우 높음** (컴퓨팅 자원 분리 및 2GB 이상 스케일링) |
| **MTTR (평균 복구 시간)** | **약 10분** (경보 감지 후 관리자의 수동 개입 및 재부팅) | **10초 이내** (ALB 헬스체크 및 Fargate Self-healing 자동 복구) |

---

## 2. Continuous Integration & Delivery (CI/CD)

### 2-1. Pipeline Workflow
본 저장소는 개발 브랜치 push부터 실서버 운영 릴리즈까지 전체 수명 주기를 제어하는 선언적 GitOps 배포 워크플로우를 가동하고 있습니다.

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

* **Path-Filtered Executions**: 마크다운 문서나 인프라 설정 단독 수정 시 Gradle/Vite 애플리케이션 컴파일 단계를 영리하게 우회하여 파이프라인 대기 시간을 단축합니다.
* **배포 동시성 제어 (Concurrency)**: Staging 배포 진행 중 추가 버그 수정 등으로 신규 커밋이 유입되는 즉시, 기존 진행 중이던 대기 상태 파이프라인을 자동 소거(`cancel-in-progress: true`)하여 리소스 경합 및 배포 순서 뒤엉킴을 방지합니다.

### 2-2. Build & Artifact Management
* **GitHub Actions & GHCR 빌드 오프로딩 (Backend)**:
  512MB RAM 서버 자원의 컴파일 병목을 방지하기 위해 백엔드 빌드 과정을 GitHub Actions Runner(7GB RAM)로 오프로딩하고, 생성된 경량 GraalVM Native 바이너리 Docker 이미지를 GitHub Container Registry (GHCR)에 버전 태그(`sha-${{ github.sha }}`) 형식으로 안전하게 푸시합니다. 운영 서버는 Docker pull만 실행하여 컨테이너 기동 오버헤드를 획기적으로 줄였습니다.
* **Vite Static Asset 동적 업로드 & 캐시 무효화 (Frontend)**:
  프론트엔드 도커 이미지 빌드/컨테이너 기동을 전면 중단하고, Actions 빌드 러너에서 정적 압축된 프론트 자산을 S3 버킷에 직접 덮어쓰기 동기화(`aws s3 sync`)한 후 CloudFront Edge Cache Invalidation을 실행해 가볍고 빠른 배포를 완수했습니다.

### 2-3. Quality Gate & Release Automation
* **Playwright 브라우저 E2E Gating**:
  Staging 환경(`stage.rogic.io`)에 자동 배포가 완료되는 즉시, Playwright 브라우저 E2E 테스트(`frontend/e2e/staging.spec.ts`)를 헤드리스 모드로 실행합니다. 홈 화면 렌더링, Nonogram Canvas 상호작용 검증, My Page 신규 유저 생성 및 XP 지표 등을 실제 브라우저 레벨에서 100% 자동 검증해 통과한 경우에만 프로모션 대기 상태로 이행합니다.
* **수동 승인 게이트 (Manual Approval Gate)**:
  Staging E2E 검증이 성공 완료되면 파이프라인이 일시 중지되며, 관리자(Admin)가 GitHub UI 상에서 직접 승인 버튼을 클릭해야만 최종 Production 인프라 프로비저닝 및 멱등적 Ansible 플레이북 배포 단계로 롤링업을 실행합니다.
* **Auto-SemVer 및 GitHub Release 자동 발행**:
  Production 배포 성공 즉시, 이전 릴리즈 태그 이후의 커밋 메시지 규칙(`feat:`, `fix:`, BREAKING CHANGE)을 동적 분석하여 메이저/마이너/패치 SemVer 버전 태그를 자동으로 연산 및 커밋하고, 관련 Changelog와 변경 노트를 작성한 GitHub Release를 완전 자동으로 발행합니다.

---

## 3. AI Engineering & Intelligent Systems

### 3-1. AI Puzzle Generator & Logical Validation Pipeline
* **Gemini API 기반 백그라운드 스케줄러**:
  넉넉한 일일 무료 할당량(500 RPD)을 지닌 `gemini-3.1-flash-lite` 모델을 Spring Boot 스케줄러와 연동했습니다. 매일 새벽 04:17에 비활성 상태(`active = false`)로 퍼즐 후보를 생성해 DB에 안전하게 보급하며, API Rate Limit 방지를 위해 각 호출 크기 간 5초 지연 시간 및 3회 자동 재시도 루프를 설계해 장애 리스크를 통제했습니다.
* **100% 논리형 퍼즐(Logical-only) 검증 파이프라인**:
  유저가 찍어서 맞추는 DFS 백트래킹(Backtracking)의 모호함 없이, 오직 논리적 유추로만 100% 풀이가 완수되는지 확인하는 `isLogicalOnly(grid)` 검증 알고리즘을 Java 백엔드 단에 구현했습니다. 1회 API 호출로 5개 후보 퍼즐 세트를 JSON Array로 수집한 후, 해당 검증을 완벽하게 통과한 고품질 퍼즐만 승인 대기 풀에 적재하고, 5회 연속 후보군 전체 실패 시 예외를 던져 데일리 데이터의 품질 정합성을 강제했습니다.
* **AOT 런타임 최적화**:
  GraalVM Native Image 구동 환경에서 Jackson이 DTO를 정상적으로 파싱할 수 있게 `AiResponseDto` 및 `StageDto`에 리플렉션 힌트([NemologicRuntimeHints.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java))를 선언해 역직렬화 오류를 차단했습니다.

### 3-2. User Feedback & Backoffice Monitoring System
* **👍 / 👎 실시간 플레이어 피드백 카드**:
  퍼즐 클리어 시 캔버스 하단 플로팅 카드 위젯(Glassmorphism 다크 테마 및 청록/적색 네온 점등 애니메이션)을 렌더링하여 유저가 해당 퍼즐의 재미와 시각적 아름다움을 직관적으로 평가할 수 있게 했습니다. 평가 완료 시 `✨ Thank You!` 메시지로 부드럽게 페이드아웃되며, 세션 ID 및 DB 동기화 예외 처리를 `finally` 구문으로 처리해 타이머 지연 현상을 원천 방어했습니다.
* **백오피스 만족도 기반 즉각 삭제**:
  클리어 피드백은 데이터베이스 `stages` 테이블의 `upvotes`, `downvotes` 컬럼에 실시간 반영되며, 관리자 화면(Backoffice) 테이블에 깔끔한 가로형 SVG Outline 피드백 지표로 실시간 수집되어 평점이 나쁘거나 형태가 훼손된 퍼즐을 관리자가 식별하여 즉시 Hard Delete(Cascade)할 수 있는 완결성 높은 운영 피드백 고리를 확보했습니다.

### 3-3. AI Agentic Development & Governance (.agents/rules)
* **AI 에이전트(Antigravity) 페어 프로그래밍**: Google DeepMind의 Advanced Agentic Coding 기술이 탑재된 자율 AI 코딩 에이전트인 `Antigravity`를 개발 전반(설계, TDD 테스트 작성, 디버깅, 인프라 배포 구성, 대시보드 스키마 교정)에 적극 페어링하여 개발 생산성을 높이고 릴리즈 주기를 단축했습니다.
* **AI 거버넌스 및 자율 조율 규격 (.agents/rules/)**:
  AI 에이전트가 단독으로 코드를 수정하거나 아키텍처 규칙을 이탈하는 것을 막기 위해, 프로젝트 정책 및 SRE 가이드라인을 규정한 규칙 파일들을 프로젝트의 `.agents/rules/` 하위에 배치하여 AI 에이전트의 작동에 완결성 높은 시스템 통제를 적용했습니다.
  * **[workflow-and-tdd.md](file:///c:/Users/82107/dev/project/nemologic/.agents/rules/workflow-and-tdd.md) (Mandatory TDD)**: 코어 비즈니스 로직(솔버, 생성기 등) 구현 전 단위 테스트 케이스 작성을 강력히 강제하고, 작업 종료 후 [docs/progress_state.md](file:///c:/Users/82107/dev/project/nemologic/docs/progress_state.md) 상태 맵을 반드시 동기화하도록 유도합니다.
  * **[architecture-and-tech-stack.md](file:///c:/Users/82107/dev/project/nemologic/.agents/rules/architecture-and-tech-stack.md) (Directory Isolation)**: 작업 시 frontend, backend, infra 간 혼재된 수정을 차단하고, Vue의 Reactivity 시스템(Ref/Reactive)이 순수 논리 물리 연산에 침범하는 것을 차단해 디커플링을 유지합니다.
  * **[safety-and-communication.md](file:///c:/Users/82107/dev/project/nemologic/.agents/rules/safety-and-communication.md) (No Guessing)**: 모호한 설계 요구 사항이 발생한 즉시 에이전트가 혼자 추측해 코드를 구현하는 행위를 전면 금지하며, 즉시 중단하고 개발자에게 직접 승인을 요구하도록 강제 조치합니다.
  * **[incident-reporting.md](file:///c:/Users/82107/dev/project/nemologic/.agents/rules/incident-reporting.md) (Postmortem Automated)**: 런타임 장애나 빌드 에러, DB 마이그레이션 실패 등 치명적 이슈가 발생하여 복구된 경우, 반드시 `docs/incidents/` 경로에 YYYYMMDD 날짜 형식으로 된 포스트모템(장애 원인 분석, 타임라인 및 재발 방지 대책) 문서를 자동 작성·보관하도록 명문화하여 AI 자율 개발 프로세스에 강력한 SRE 규범을 바인딩했습니다.

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
