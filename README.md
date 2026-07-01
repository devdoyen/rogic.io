# 0. Project Overview

본 저장소는 `rogic.io` 프로젝트의 빌드 및 배포에 필요한 CI/CD 파이프라인, IaC 기반 인프라 구성 코드(Terraform/Ansible), 그리고 모니터링 환경의 구축 명세를 담고 있습니다.

## 0.1. Game Concept

`rogic.io`는 전통적인 사각형 격자판에서 퍼즐을 해결하는 네모로직(노노그램) 게임입니다. 단, 출제 시점에 임의의 각도로 회전된 퍼즐을 해결하면, 완료되는 순간 원래 방향으로 자동 회전 복원되며 완성된 패턴을 올바르게 보여주는 메커니즘을 내장하고 있습니다.

<p align="center">
  <img src="./docs/assets/rogic_gameplay_demo.webp" width="70%" alt="rogic.io Gameplay Demo" />
</p>

## 0.2. Service Environments

| Service Environment | Live URL | Deployment Status |
| :--- | :--- | :--- |
| 🚀 **Production** | [rogic.io](https://rogic.io) | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) |
| 🧪 **Staging** | [stage.rogic.io](https://stage.rogic.io) | ![Active](https://img.shields.io/badge/Status-Active-blue) |

## 0.3. Technology Stack
| Category | Technologies | Description |
| :--- | :--- | :--- |
| **Frontend** | `Vue 3`, `TypeScript`, `HTML5 Canvas API`, `Axios` | Client app with decoupled pure TS game engine. |
| **Backend** | `Java 17`, `Spring Boot`, `Spring Data JPA` | REST API layer for stage state, history, and users. |
| **Database** | `PostgreSQL 16` | Relational storage for user logs, clear history, and stages. |
| **Infra & IaC** | `AWS`, `Terraform`, `Ansible`, `Docker Compose` | Code-defined AWS resources & automated config deployment. |
| **CI/CD** | `GitHub Actions`, `Vitest`, `Playwright` | Path-filtered tests, browser E2E validation, auto-SemVer. |
| **Telemetry** | `Prometheus`, `Grafana Cloud`, `CloudWatch` | Agentless scraping, log alarms, SNS email alerting. |

---

# 1. Infrastructure

## 1.1. System Architecture

### 1.1.1. High-Level Diagram
```mermaid
C4Context
    title System Context Diagram for rogic.io (Level 1: System Context)

    Person(player, "Player / User", "Accesses the puzzle game through a web browser.")
    
    System_Boundary(dns_cdn, "Global Edge Delivery") {
        System_Ext(route53, "Route 53", "DNS management mapping domains to CloudFront/EC2.")
        System_Ext(cloudfront, "Amazon CloudFront", "CDN distributing static web assets globally.")
        System(s3, "Amazon S3 Bucket", "Stores Vite-built Vue static compilation files.")
    }

    System_Boundary(backend, "Core API Server") {
        System(api, "rogic.io REST API", "Spring Boot backend handling gameplay, XP levels, and leadership stats.")
        SystemDb(postgres, "PostgreSQL DB", "Relational database storing user logs, statistics, and stage metadata.")
    }

    Rel(player, route53, "Queries DNS entries", "DNS Protocol")
    Rel(player, cloudfront, "Requests static assets (HTML/JS/CSS)", "HTTPS / Port 443")
    Rel(cloudfront, s3, "Pulls origin static files", "S3 Protocol")
    Rel(player, api, "Calls REST API services", "HTTPS / Port 443")
    Rel(api, postgres, "Reads/Writes game state", "JDBC & JPA / Port 5432")
```

### 1.1.2. Component Specification
* **Frontend Static Hosting**<br>
  Vite 컴파일 결과물을 `Amazon S3` 버킷(OAC 차단)에 호스팅하고, `Amazon CloudFront` CDN을 통해 정적 웹 리소스를 배포합니다.
* **Backend API Gateway**<br>
  Spring Boot 애플리케이션을 단일 EC2 인스턴스 내 Docker 컨테이너로 가동하며, 프론트엔드 레벨에는 Nginx 리버스 프록시를 배치하여 `api.rogic.io` / `api.stage.rogic.io` 경로에 SSL/TLS 종단 처리를 수행합니다.
* **Telemetry Proxy**<br>
  수집 데몬(Alloy) 설치를 배제하고 Nginx Bearer 토큰 검증을 이용해 Prometheus Actuator 엔드포인트를 외부에 간접 노출하여 수집 부하를 제거했습니다.

---

## 1.2. Cost Optimization
* **인프라 월간 운영 비용 분석 (Monthly Billing Summary)**<br>
  자원 다중화 및 관리형 DB 서비스 대신 가상 컨테이너 기술과 복구 지향형 설계를 연동하여 월 $11.45 (세후 실청구액 기준, 기존 대비 약 80% 비용 절감)의 상용 인프라 운영을 달성했습니다.

  | 구분 (Category) | 기존 구성 예상 비용 (Estimated) | 최적화 구성 실제 비용 (2026년 6월) | 주요 비고 (Key Notes) |
  | :--- | :--- | :--- | :--- |
  | **컴퓨팅 및 스토리지** | $20.00 / 월 (t3.micro) | $5.50 / 월 (t3a.nano + EBS) | GraalVM 네이티브 컨테이너화를 통해 메모리 스레싱 극복 |
  | **로드 밸런서** | $20.00 / 월 (AWS ALB) | $0.00 / 월 (Self-hosted Nginx) | ALB 제거 후 Route 53 고정 EIP 다이렉트 매핑 |
  | **데이터베이스** | $15.00 / 월 (RDS PostgreSQL) | $0.00 / 월 (PostgreSQL Container) | EC2 호스트 내부 Docker Compose 환경 가동 |
  | **네트워크 & 도메인** | - | $4.74 / 월 (IP 주소 + Route 53) | 퍼블릭 IPv4 사용료 ($3.70) + 호스팅 영역 ($1.04) |
  | **기타 (데이터 전송 등)** | - | $1.21 / 월 | 데이터 트래픽 전송 및 유틸리티 자원 비용 |
  | **합계 (Total)** | **약 $55.00 / 월** | **총 $11.45 / 월** | **기존 대비 약 80% 비용 절감 달성 (세후 실청구액)** |

### 1.2.1. Compute Resource Downsizing
* **t3a.nano/t4g.nano (512MB RAM) 타겟팅**<br>
  월 $3.5 대 컴퓨팅 인스턴스 사양에 맞추어 리소스를 튜닝했습니다.
* **GraalVM Native Image 메모리 최적화**<br>
  런타임 메모리 사용량을 컨테이너당 30MB 이하로 낮추어, 초경량 컴퓨팅 환경 내에서도 배포 시 두 버전의 Spring Boot 컨테이너를 함께 띄울 수 있는 기반을 다졌습니다.
  * **Jackson 역직렬화 DTO Reflection 힌트**<br>
    Native 빌드 오류 방지를 위해 [NemologicRuntimeHints.java](backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java)에 리플렉션 힌트를 명시했습니다.
* **Docker Garbage Collection 자동화**<br>
  디스크 용량 고갈 장애 예방을 위해 새벽 3시마다 72시간 경과 도커 리소스를 강제 소거하는 prune 스크립트를 크론탭으로 자동 배치했습니다.

### 1.2.2. Load Balancer Elimination
* **ALB 제거 및 고정 EIP 구성**<br>
  월 $20 상당의 AWS ALB를 배제하고 DNS 도메인(Route 53)과 고정 Elastic IP를 매핑했습니다.
* **EC2 Auto Recovery 및 복구 지향 아키텍처(ROA)**<br>
  ALB 부재에 따른 장애 전파를 줄이기 위해 시스템 알람 연동 호스트 자동 복구(Auto Recovery)를 결합하고, 재해 복구 시 IaC 코드를 활용해 5분 이내 인프라를 복원하도록 구성했습니다.

### 1.2.3. Database Cost Minimization
* **Self-hosted PostgreSQL 컨테이너**<br>
  월 $15~20 이상의 RDS 비용을 아끼기 위해 EC2에 DB 컨테이너를 기동했습니다.
* **S3 정기 백업 및 Lifecycle 제어**<br>
  6시간 주기로 DB dump 데이터를 S3로 업로드하는 쉘 스크립트와 Cron을 배포하고, S3 백업 버킷에 30일 경과 백업 자동 파기 정책을 적용했습니다.

---

## 1.3. Technical Trade-offs
비용 최적화를 달성하기 위해 포기한 기술적 혜택(Trade-offs)과 이를 극복하기 위해 설계한 완화 대책(Mitigations)을 명시적으로 투명하게 공개합니다.

### 1.3.1. Build Resource Constraints
* **물리 메모리 고갈에 따른 컴파일 리스크 (Trade-off)**<br>
  t3a.nano(512MB RAM) 환경에서는 메모리 제약으로 인해 서버 내에서 직접 GraalVM 컴파일 빌드가 불가능하며, 빌드 속도 또한 JVM 컴파일에 비해 10배 이상 오래 소요됩니다.
* **외부 컴퓨팅 오프로딩 (Mitigation)**<br>
  CI/CD 파이프라인에서 GitHub Actions가 제공하는 외부 빌드 인프라(2 Core, 7GB RAM)에 컴파일 연산 부하를 위임하고, 운영 서버 호스트는 30MB 수준의 무부하 바이너리 구동만 전담하도록 분리 구조화했습니다.

### 1.3.2. Single Point of Failure (SPOF)
* **다중 AZ 로드밸런싱 포기 (Trade-off)**<br>
  AWS Load Balancer(ALB) 배제로 인해 다중 가용구역(Multi-AZ) 무중단 이중화 및 롤링 배포를 달성할 수 없으며, 호스트 물리 장애 시 전체 정전이 발생하는 단일 장애점(SPOF)을 노출하게 됩니다.
* **호스트 자동 복구 결합 (Mitigation)**<br>
  AWS CloudWatch Status Check Metric Alarms를 결합해 물리 하드웨어 결함 발생 시 1분 이내에 인스턴스를 정상 물리 호스트로 자동 복원(Auto Recovery)하여 EIP를 바인딩하도록 인프라 복원력을 강화했습니다.

### 1.3.3. Recovery Indicators (RTO / RPO)
* **관리형 DB Failover 및 시점 복구 상실 (Trade-off)**<br>
  AWS RDS의 완전관리형 이중화 복구(RTO 0초 타겟) 및 시점 복구(RPO 5분 이내 PITR) 편의성을 누릴 수 없으며, 재해 복구 시 백업 덤프 수동 복원이 필요하므로 RTO/RPO 지표가 수 분에서 최대 6시간 수준으로 후퇴합니다.
* **복구 지향 아키텍처(ROA) 구현 (Mitigation)**<br>
  인프라를 코드로 구성(Terraform/Ansible)하여 재설치 과정을 자동화하고, 6시간 주기 백업 덤프 자산을 독립 버킷 S3에 안전하게 보관하여 전체 데이터 유실 및 가상 머신 소멸 시에도 5분 이내 수동 복구 가능한 절차를 수립했습니다.

---

## 1.4. Security Infrastructure

### 1.4.1. Network Isolation
```mermaid
C4Container
    title Container Diagram for rogic.io (Level 2: Network & Containers)

    Person(player, "Player / User", "Accesses the puzzle game through a web browser.")

    System_Boundary(aws, "AWS Cloud (ap-northeast-2)") {
        
        System_Boundary(vpc_prod, "Production VPC (10.0.0.0/16)") {
            Container(nginx, "Nginx Reverse Proxy", "Docker Container", "SSL/TLS termination, API routing, and Bearer token auth validation.")
            Container(spring, "Spring Boot App", "Docker Container (GraalVM)", "Handles business logic, daily puzzle scheduling, rating, and XP leaderboard.")
            ContainerDb(postgres, "PostgreSQL Database", "Docker Container", "Persists puzzle templates, user logs, clear history, and user stats.")
            
            Rel(nginx, spring, "Proxy API requests", "HTTP / Port 8080")
            Rel(spring, postgres, "Reads/Writes state", "JPA & JDBC / Port 5432")
        }
        
        System_Boundary(vpc_stage, "Staging VPC (10.1.0.0/16)") {
            Container(nginx_stg, "Nginx Reverse Proxy (Stage)", "Docker Container", "Staging SSL/TLS termination and API routing.")
            Container(spring_stg, "Spring Boot App (Stage)", "Docker Container (JVM)", "Staging application runtime environment.")
            ContainerDb(postgres_stg, "PostgreSQL Database (Stage)", "Docker Container", "Persists isolated staging state.")
            
            Rel(nginx_stg, spring_stg, "Proxy API requests", "HTTP / Port 8080")
            Rel(spring_stg, postgres_stg, "Reads/Writes state", "JPA & JDBC / Port 5432")
        }

        Container(cloudfront, "Amazon CloudFront", "AWS CDN", "Distributes static web assets with low latency.")
        Container(s3, "Amazon S3", "AWS Bucket Storage", "Hosts Vite/Vue built static files (HTML, JS, CSS).")
    }

    Rel(player, cloudfront, "Fetches static web pages", "HTTPS / Port 443")
    Rel(cloudfront, s3, "Refreshes cache from origin", "S3 Protocol")
    Rel(player, nginx, "Calls API endpoints", "HTTPS / Port 443")
    Rel(player, nginx_stg, "Calls API endpoints (Stage)", "HTTPS / Port 443")
```

* **물리 격리형 VPC 구성**<br>
  Staging VPC(`10.1.0.0/16`)와 Production VPC(`10.0.0.0/16`)를 개별 서브넷 대역과 독립 인프라망으로 분리 프로비저닝하여 상호 간의 간섭을 완전히 격리했습니다.

### 1.4.2. Access Control
* **보안 그룹 최소화 권장**<br>
  SSH(22), Nginx HTTP/S(80/443), Spring(8080) 이외의 외부 불필요한 포트 인바운드를 SG 방화벽 규칙을 통해 원천적으로 차단했습니다.

### 1.4.3. SSL/TLS Certificate Management
* **Let's Encrypt 및 Certbot 갱신**<br>
  HTTPS(443) 통신 및 HTTP(80) 301 리다이렉트 정책을 구현하였으며, 3개월 만료 인증서 자동 갱신을 지원하는 pre/post 쉘 스크립트 훅을 Certbot 데몬에 바인딩했습니다.

### 1.4.4. State Management Security
* **테라폼 원격 상태 잠금**<br>
  AWS S3 버킷과 DynamoDB 테이블(`LockID`)을 Backend로 연동해 개발자 배포 시 형상 관리(State)의 동시 수정 충돌을 원천 방지했습니다.

---

## 1.5. Observability

### 1.5.1. Metric Collection & Scraping
```mermaid
C4Container
    title Telemetry Diagram for rogic.io (Level 3: Observability & Alerting)

    System_Boundary(host, "AWS EC2 Instance (Target Host)") {
        Container(nginx, "Nginx Reverse Proxy", "Docker", "Bearer Token Authentication Endpoint.")
        Container(spring, "Spring Boot Backend", "Docker (GraalVM)", "Exposes Prometheus Actuator Metrics.")
        Rel(nginx, spring, "Forwards prometheus scraping requests", "Port 8080")
    }

    System_Boundary(grafana_cloud, "Grafana Cloud Platform") {
        Container(grafana, "Grafana Dashboards", "SaaS Dashboard", "Visualizes SLA metrics, CPU, Memory, and log groups.")
        Container(prometheus, "Prometheus / Mimir", "SaaS TSDB", "Scrapes metrics via Agentless Pull architecture.")
        Rel(grafana, prometheus, "Queries metrics data")
    }

    System_Boundary(observability, "AWS Management & Alerting") {
        Container(cw, "Amazon CloudWatch", "AWS Logging", "Collects application stdout log streams via awslogs driver.")
        Container(sns, "AWS SNS Topic", "AWS Alerting", "Triggers notifications based on metric filter threshold alarms.")
        Person(sre, "SRE Developer", "Receives real-time incident warning emails.")
        
        Rel(cw, sns, "Metric Filter Threshold Alarmed")
        Rel(sns, sre, "Sends warning email notification")
    }

    Rel(prometheus, nginx, "Scrapes metrics (Agentless Pull)", "HTTPS Bearer Auth / Port 443")
    Rel(spring, cw, "Streams application logs", "awslogs driver")
```

* **Agentless Pull 아키텍처**<br>
  호스트 리소스를 소모하는 수집기(Alloy) 대신, Nginx 프록시가 `Authorization: Bearer` 헤더 토큰을 대조 검증하는 가상 경로를 열고 외부 Grafana Cloud Mimir가 직접 긁어가도록 구조화했습니다.

### 1.5.2. Centralized Log Management
* **awslogs Docker 드라이버 연동**<br>
  컨테이너 출력을 AWS CloudWatch Logs(`/aws/ec2/nemologic`)로 실시간 포워딩하여 디스크 점유율을 줄였으며, 헬스체크 및 메트릭 수집 API 경로는 Nginx Access Log에서 제외(off) 처리했습니다.

### 1.5.3. Alerting & Notification
* **장애 감지 경보 연동**<br>
  CloudWatch Logs Metric Filter 오류 발생 시 AWS SNS를 경유해 개발자 메일로 상황이 실시간 통보되며, 도쿄·싱가포르·시드니 리전에서 동시에 `/actuator/health` 헬스체크 실패가 감지되면 Grafana 경보가 트리거됩니다.

### 1.5.4. SLO (Service Level Objective) Visualization
* **통합 관제 SLA 대시보드 ([current_dashboard.json](infra/monitoring/current_dashboard.json))**<br>
  SRE 핵심 품질 지표(Uptime SLA, Incident Count, MTTR, MTBF)를 Grafana 전역 시간 범위(Time Range Picker)에 동적으로 연동되도록 설계하여 단일 행 4열 KPI 카드 레이아웃에 맞춰 배치했습니다.
* **[Grafana Live Public Dashboard](https://grandwalrus3189.grafana.net/public-dashboards/ec9e06b0d1ea4540b97af6b56abb1380)**<br>
  레이아웃 구성 예시용 퍼블릭 링크 (보안 정책 상 실제 메트릭 데이터 대신 구조 확인용 임의 지표가 노출됩니다.)

#### [부록 1] SLA 지표 PromQL 연산 수식
> [!NOTE]
> 수식 내 기호 정의: $P_t \in \{0, 1\}$는 특정 측정 시점 $t$의 API 헬스체크 가용 성공 여부(`probe_success`)를 의미합니다.

* **API Health Status**

$$\text{API Health} = \sum P_t$$

```promql
sum(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"})
```

* **Dynamic Service Availability**

$$\text{Availability (\%)} = \text{avg}_{t \in \text{range}}(P_t) \times 100$$

```promql
avg_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) * 100
```

* **Dynamic Incident Count**

$$\text{Incident Count} = \frac{\text{changes}(P_t)}{2}$$

```promql
changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2
```

* **Dynamic MTTR (Mean Time To Recovery)**

$$\text{MTTR (sec)} = \frac{\left(\text{count}_{t \in \text{range}}(P_t) - \sum_{t \in \text{range}} P_t\right) \times 60}{\max\left(\frac{\text{changes}(P_t)}{2}, 1\right)}$$

```promql
((count_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) - sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range])) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2, 1)
```

* **Dynamic MTBF (Mean Time Between Failures)**

$$\text{MTBF (sec)} = \frac{\sum_{t \in \text{range}} P_t \times 60}{\max\left(\frac{\text{changes}(P_t)}{2}, 1\right)}$$

```promql
(sum_over_time(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) * 60) / clamp_min(changes(probe_success{job="nemologic-api-health", instance="https://rogic.io/actuator/health"}[$__range]) / 2, 1)
```

#### [부록 2] 가용성 및 재해 복구 지표 비교표
| 지표 | 현재 사양 (단일 EC2 + S3 백업) | 향후 개선 목표 (Multi-AZ ALB + ECS/RDS) |
| :--- | :--- | :--- |
| **RPO (복구 시점)** | **6시간** (하루 4회 S3 백업 소산) | **5분 이내** (RDS Multi-AZ 및 PITR 자동 활성화) |
| **RTO (복구 시간)** | **약 20분** (Terraform 프로비저닝 복구 및 DB 덤프 복원) | **1분 이내** (ALB 액티브 백업 및 컨테이너 무중단 교체) |
| **MTBF (평균 고장 간격)** | **낮음** (t3a.nano 노드 리소스 병목 리스크 존재) | **매우 높음** (컴퓨팅 자원 분리 및 2GB 이상 스케일링) |
| **MTTR (평균 복구 시간)** | **약 10분** (경보 감지 후 관리자의 수동 개입 및 재부팅) | **10초 이내** (ALB 헬스체크 및 Fargate Self-healing 자동 복구) |

---

## 1.6. Troubleshooting

### 1.6.1. 호스트 메모리 고갈 장애 복구
* **배경**<br>
  인프라 비용 극 최소화(월 $11.45 구성)를 위해 t3a.nano 인스턴스(512MB RAM) 환경을 선택하였으나, 모니터링 수집 에이전트(Grafana Alloy)의 메모리 점유(100MB+)와 블루/그린 배포 시점에 Spring Boot 컨테이너 2개가 일시적으로 동시에 기동하면서 물리 메모리 한계를 초과하여 OOM 및 CPU 스레싱 장애가 빈번히 발생함.
* **해결 방안**<br>
  - **Agentless Pull 아키텍처 도입**<br>
    호스트 리소스를 차지하는 수집 데몬(Alloy)을 배제. 대신 Nginx 리버스 프록시 단에서 Spring Actuator 메트릭 엔드포인트를 Bearer 토큰 보안 검증 하에 외부 노출하고, Grafana Cloud Prometheus가 원격으로 Pull(Scraping)하게 전환하여 모니터링 에이전트 구동에 따른 메모리 점유를 제거함.
  - **GraalVM Native Image 고도화**<br>
    빌드 타임 AOT 컴파일 및 Jackson 리플렉션 힌트 지정을 통해 Spring Boot 컨테이너 런타임 메모리 풋프린트를 기존 250MB+에서 **30MB 이하**로 극소화하여, 512MB RAM의 가혹한 물리 환경에서도 2개 컨테이너 무중단 교체 가용성을 안정적으로 유지함.
* **개발자 회고 (Retrospective)**<br>
  - 이 문제 해결 과정에서 코딩 AI 에이전트는 `t3a.nano` 환경의 리소스 임계치를 근거로 인스턴스 스케일업(micro/small로 업그레이드) 및 표준 관리형 아키텍처(ALB, RDS) 도입을 강력히 권장했습니다.
  - 물론 정석적인 모범 사례(Best Practice)에 따르는 편이 쉬운 길이었겠으나, **극단적인 비용 효율화와 한계 최적화**라는 프로젝트의 기술적 지향점을 지키기 위해 기술적 수단을 집요하게 모색했습니다.
  - 그 결과, 메트릭 수집 방식을 Push에서 Pull로 전환하고 GraalVM 메모리 풋프린트를 30MB 이하로 튜닝하는 등 깊이 있는 시스템 최적화 경험을 축적할 수 있었습니다. 도구(AI)의 제안을 맹신하지 않고, 프로젝트 상황에 맞게 주도적으로 아키텍처의 트레이드오프를 결정하는 역량의 중요성을 깨닫게 해준 값진 트러블슈팅 사례입니다. (자세한 인시던트 과정은 [Incident Report 2026-07-01](./docs/incidents/20260701_daily_puzzle_generation_failure.md) 참고)

---

# 2. CI/CD

## 2.1. Pipeline Workflow

### 2.1.1. GitOps Flowchart
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

### 2.1.2. Pipeline Trigger Optimization
* **경로 필터(Path Filtering)**<br>
  단순 문서나 로컬 마크다운 수정 커밋 유입 시에는 빌드/컴파일 단계를 스킵하여 배포 속도를 최적화했습니다.
* **배포 대기 취소(Concurrency)**<br>
  Staging 진행 중 추가 커밋이 수입되는 즉시 이전 배포 작업을 강제 취소(`cancel-in-progress: true`)해 배포의 꼬임 현상을 방지했습니다.

---

## 2.2. Artifact Management

### 2.2.1. Compute Offloading
* **Actions Runner 컴파일 오프로딩**<br>
  512MB 호스트 내부의 컴파일 한계를 극복하기 위해 GitHub Actions Runner(7GB RAM) 환경에서 GraalVM Native AOT 컴파일을 수행하고 완료 이미지(`sha-${{ github.sha }}`)를 GHCR에 업로드해 운영 노드의 메모리 부하를 방지했습니다.

### 2.2.2. Static Asset Delivery
* **Vite Static Asset 동적 업로드**<br>
  프론트엔드는 도커 이미지 생성 대신 컴파일된 정적 자산(index.html, JS 번들)을 S3 버킷으로 다이렉트 동기화(`aws s3 sync`)하고 CloudFront Edge 무효화(Invalidation)를 호출하는 초경량 정적 호스팅 배포 방식을 수립했습니다.

---

## 2.3. Release Automation

### 2.3.1. Automated End-to-End Testing
* **Playwright E2E 테스트**<br>
  Staging 배포 완료 즉시 Playwright 브라우저(`frontend/e2e/staging.spec.ts`)를 헤드리스 기동하여 홈 화면 로딩, 캔버스 노노그램 상호작용 및 익명 가입 로직을 실제 유저 브라우저 환경에서 자동 점검해 품질 게이트를 가동합니다.

### 2.3.2. Deployment Gate & Approvals
* **수동 승인 배포(Manual Gate)**<br>
  Staging 테스트가 100% 성공하면 빌드를 일시 정지시키고 관리자가 직접 GitHub Environment 상에서 릴리즈를 검증/승인해야만 Production으로 롤링 배포를 승격시키는 안전 장치를 구성했습니다.

### 2.3.3. Automated Versioning
* **Auto-SemVer 및 Release 자동 작성**<br>
  커밋 메시지 토큰(`feat:`, `fix:`) 규격을 파싱해 SemVer 버전을 갱신하고, 변경 이력(Changelog) 작성과 GitHub Release 릴리즈 발행 과정을 100% 자동화했습니다.

---

## 2.4. Troubleshooting

### 2.4.1. 배포 파이프라인 충돌 오류 극복
* **배경**<br>
  - Staging과 Production 인프라 설정이 동일 Terraform 코드에 묶여 일괄 반영되던 중, 운영 환경 S3 버킷에 정적 자산이 시딩되지 않은 상태에서 DNS A 레코드가 CloudFront/S3로 먼저 스위칭되어 운영 전체 접속 차단(`AccessDenied`) 장애 발생 ([Access Failure Report](./docs/incidents/20260630_production_access_failure.md)).
  - 핫픽스 도중 GitHub Actions의 `cancel-in-progress: true` 설정으로 인해 Nginx 인증서 발급 프로세스 도중 후속 커밋이 이전 빌드를 강제 취소하면서 실서버 SSL 인증서 유실로 인한 HTTPS API 통신 불능 장애 발생 ([Handshake Failure Report](./docs/incidents/20260630_production_api_handshake_failure.md)).
* **해결 방안**<br>
  - **인프라 환경 물리 격리**<br>
    Terraform Workspace 및 디렉토리 구조를 Staging과 Production으로 엄격히 분할하여 단일 실행이 실 운영계에 즉각 영향을 미치지 않도록 조치.
  - **중요 배포 동시성 차단 옵션 제거**<br>
    중요 서버 설정 배포 단계(`deploy-production`)에서 `cancel-in-progress: false`를 명시하여 이전 작업이 중도 파기되는 설정 정합성 훼손을 원천 차단.
  - **배포 단계의 느슨한 결합(Loose Coupling)**<br>
    CloudFront TLS 및 SSL 인증서 교체 등의 상호 의존적인 작업들이 실제 서버 준비 상태를 검증한 후에 이루어지도록 수동 승인 게이트(Manual Approval Gate)를 도입해 인프라 프로모션 방식을 개선함.

---

# 3. AI Engineering

## 3.1. AI Puzzle Pipeline
* **자동 생성 스케줄러**:
  새벽 04:17마다 `gemini-3.1-flash-lite` LLM API를 호출하여 신규 퍼즐 레이아웃을 생성하며, API Rate Limit 방어를 위해 5초의 지연 간격과 3회의 재시도 장치를 백엔드에 장착했습니다.
* **논리해 자동 검증**:
  AI가 생성한 퍼즐 중 단순 찍기로 풀 수 있는 오출제 건을 필터링하기 위해, Java 백엔드 단에 DFS 백트래킹 기반의 `isLogicalOnly(grid)` 추론 검증 알고리즘을 이식하여 무결성 품질 가드레일을 구축했습니다.

---

## 3.2. AI Governance
* **피드백 루프**:
  유저의 평점(👍/👎) 피드백 카드가 DB `stages` 테이블에 기록되며, 백오피스 대시보드에서 평점 현황을 식별할 수 있습니다.
* **수동 강제 삭제**:
  품질 미달이나 기형적인 퍼즐로 판단되면, 관리자가 백오피스 대시보드에서 연쇄 삭제(Cascading Delete)를 통해 즉각 Hard Delete할 수 있는 관리 장치를 도입했습니다.
* **에이전트 거버넌스 규칙 (.agents/rules/)**:
  AI 코딩 에이전트와 협업하여 지속 가능한 리스크 관리 및 고신뢰성 코딩 컨벤션을 준수하기 위해 정의된 파일 목록입니다:

  | 규칙 파일 | 주요 관리 목적 및 정책 요약 | 형상 추적 여부 |
  | :--- | :--- | :--- |
  | [architecture-and-tech-stack.md](.agents/rules/architecture-and-tech-stack.md) | 프론트/백엔드/인프라 레이어의 다중 동시 수정 차단, Vue Reactivity 논리 유출 방지, 순차 배포 준수 | `Git Tracked` |
  | [documentation-guidelines.md](.agents/rules/documentation-guidelines.md) | 상대경로(file:// 금지) 사용, 마크다운 개행 규정 준수, 비교 수치 데이터 기술 시 테이블(Table) 시각화 의무화 | `Git Tracked` |
  | [git-and-commit-guidelines.md](.agents/rules/git-and-commit-guidelines.md) | Conventional Commits 규칙 준수, 로컬 커밋 자동 보존 및 원격 push 개발자 위임 | `Git Tracked (Force Added)` |
  | [workflow-and-tdd.md](.agents/rules/workflow-and-tdd.md) | 코어 로직 작성 시 TDD(Test-Driven Development) 선행 의무화 및 progress_state.md 수시 동기화 | `Git Tracked` |
  | [safety-and-communication.md](.agents/rules/safety-and-communication.md) | 요구사항이 모호한 경우 임의 구현(No Guessing)을 중단하고 개발자 승인 대기 | `Git Tracked` |
  | [incident-reporting.md](.agents/rules/incident-reporting.md) | 장애 리포트 작성 시 3W1H 사상에 근거한 구체적 원인-결과 수치 명세 및 포스트모템 구조화 | `Git Tracked` |

---

## 3.3. Troubleshooting

### 3.3.1. AI 생성 퍼즐 파싱 장애 복구
* **배경**<br>
  초경량 LLM 모델이 30x30 대형 그리드 생성 시 응답 지연을 아끼기 위해 JSON 포맷 대신 `Array(30).fill(0)` 같은 JS 문법을 변형 반환하여 백엔드 Jackson 역직렬화 오류(`JsonParseException`) 및 배치 스케줄러 중단 장애 발생 ([Daily Puzzle Failure Report](./docs/incidents/20260701_daily_puzzle_generation_failure.md)).
* **해결 방안**<br>
  AI 프롬프트에 `MUST be a literal 2D JSON array` 제약 가드레일을 주입하고, 대형 퍼즐 생성 시 출력 토큰 안전성 확보를 위해 후보군(Candidate) 개수를 5개에서 2개로 축소 조절하여 파싱 신뢰성을 100%로 확보함.

---

# 4. Appendices

## 4.1. Local Development Setup
To run `rogic.io` on your local workstation, select one of the options below:

### Option 1: Docker Compose (Single-Command Spin Up - Recommended)
If you want to build and spin up the entire application stack (Database, Backend, and Frontend) immediately:

```bash
# In the project root, compile, build and start all container services
docker compose up --build
```
* **Frontend Web Client**: `http://localhost:5173`
* **Backend REST API**: `http://localhost:8080`
* **Prerequisites**: Docker & Docker Compose installed.

---

### Option 2: Hybrid Local Setup (For Hot-Reloading Development)
If you want to modify code on the fly and take advantage of live hot-reloading (Vite dev server):

#### Step 1: Start PostgreSQL Database
```bash
# Start only the database container in the background
docker compose up -d db
```

#### Step 2: Run Backend API
```bash
cd backend
./gradlew bootRun
```
* API Server will run on: `http://localhost:8080`
* **Prerequisites**: Java 17 JDK installed.

#### Step 3: Run Frontend Client
```bash
cd frontend
npm install
npm run dev
```
* Frontend app will run on: `http://localhost:5173`
* **Prerequisites**: Node.js 20+ installed.


