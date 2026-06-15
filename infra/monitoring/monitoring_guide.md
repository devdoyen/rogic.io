# Grafana Cloud Synthetic Monitoring 및 이메일 알림 연동 가이드

본 문서는 Grafana Cloud의 무료 티어를 활용하여 서비스 가동률(Availability)을 측정하고, 장애 발생 시 개발자 이메일로 경보 알림을 전송하는 모니터링 시스템을 구축하는 가이드입니다.

---

## 1. Synthetic Monitoring HTTP Check 설정

Grafana Cloud 웹 콘솔에서 외부 검증용 헬스체크 프로브를 생성합니다.

1. **Grafana Cloud 로그인**: [Grafana Cloud portal](https://grafana.com/products/cloud/)에 접속 및 로그인합니다.
2. **Synthetic Monitoring 메뉴 진입**: 좌측 네비게이션 바에서 **Home > Testing & synthetics > Synthetics** 메뉴로 이동합니다.
3. **HTTP Check 생성**:
   * **Initialize** 단계가 나오면 활성화를 완료합니다.
   * **Checks** 탭에서 **Add check** 버튼을 클릭하고 **HTTP** 타입을 선택합니다.
4. **Check 세부 정보 설정**:
   * **Job name**: `nemologic-api-health`
   * **Target URL**: `https://rogic.io/actuator/health`
   * **Frequency**: `60s` (무료 쿼터 내 가동을 위해 60초 주기 권장)
   * **Timeout**: `5s`
5. **프로브 위치(Probes) 선택**:
   * 아시아권 최적 측정을 위해 **Tokyo (ap-northeast-1)**, **Singapore (ap-southeast-1)**, **Seoul (ap-northeast-2, 지원 시)** 등을 포함하여 전 세계 3개 이상의 Probes를 활성화합니다.
6. **Save**: 설정 완료 후 **Save check**를 클릭합니다.

---

## 2. 이메일 수신처 및 알림 정책 연동 (Alerting)

서버 다운 시 지정된 이메일 주소로 긴급 장애 보고를 받기 위한 알림 채널을 설정합니다.

### ① 연락처 및 경보 규칙 (Terraform 자동 생성)
* [grafana.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/grafana.tf) 코드에 의해 연락처(`Developer-Email-Alerts`) 및 경보 규칙(`Nemologic-Service-Down-Alert`, `severity=critical` 레이블 포함)이 **Terraform Apply 시점에 자동으로 생성**되므로 UI에서 별도로 입력하지 않으셔도 됩니다.

### ② 알림 라우팅 정책(Notification Policy) 매핑 (수동 연결)
다른 모니터링 프로젝트와의 알림 간섭을 피하기 위해, 테라폼으로 생성된 연락처와 경보 규칙을 그라파나 UI상에서 안전하게 상호 연결해 줍니다.
1. Grafana 콘솔 메뉴에서 **Alerting > Notification policies**로 이동합니다.
2. **Specific routing policies** 섹션 아래에 있는 **New policy** 버튼을 클릭합니다.
3. **Matching labels**:
   * Label: `severity`
   * Operator: `=`
   * Value: `critical` (테라폼 규칙에 정의된 레이블과 매핑됩니다)
4. **Contact point**: 테라폼이 자동 생성한 **`Developer-Email-Alerts`** 연락처를 선택합니다.
5. **Save policy**를 클릭하여 설정을 저장합니다.


---

## 3. SLA & 가용성 대시보드 구축 (Terraform 자동 배포 및 수동 설정)

수집된 Synthetic Monitoring 메트릭을 기반으로 한눈에 장애 품질 지표를 볼 수 있는 Grafana 대시보드를 생성합니다.

### ① Terraform 기반 자동 프로비저닝 (추천)
* [grafana.tf](file:///c:/Users/82107/dev/project/nemologic/infra/terraform/grafana.tf) 코드에 의해 [sla_dashboard.json](file:///c:/Users/82107/dev/project/nemologic/docs/sla_dashboard.json) 템플릿 모델이 자동으로 로드되고, 스택 내의 Prometheus 데이터소스 고유 ID(UID)를 조회하여 맵핑을 완료한 후, `Nemologic Monitoring` 폴더 내에 대시보드가 **자동으로 배포(Apply)**됩니다. 따라서 별도의 임포트 단계 없이 그라파나 대시보드 메뉴에서 바로 확인이 가능합니다.

### ② 1-Click 수동 대시보드 임포트 (Terraform 미사용 시)
Terraform을 사용하지 않는 경우, 레포지토리에 사전 패키징된 [sla_dashboard.json](file:///c:/Users/82107/dev/project/nemologic/docs/sla_dashboard.json) 파일을 다운로드해 2단계로 즉시 생성할 수 있습니다.
1. Grafana Cloud 메뉴에서 **Dashboards > New > Import**를 클릭합니다.
2. **Upload dashboard JSON file** 버튼을 눌러 `sla_dashboard.json` 파일을 업로드합니다.
3. 데이터소스 변수(`DS_PROMETHEUS`) 선택 란에서 **grafanacloud-prom** 데이터소스를 선택한 후 **Import**를 누릅니다.

### ③ 수동으로 패널 추가 시 (커스텀 빌드용)
수동으로 대시보드 패널을 설계할 경우 아래의 PromQL 쿼리 및 설정을 기반으로 패널들을 직접 추가합니다.

### 패널 1: 실시간 가동 여부 (Stat Panel)
* **Title**: `API Health Status`
* **PromQL Query**:
  ```promql
  sum(probe_success{job="nemologic-api-health"})
  ```
* **Options**: Stat 패널 선택, Thresholds 지정 (0: Red, 1 이상: Green).

### 패널 2: 가용성 백분율 (Gauge/Stat Panel - Uptime SLA)
* **Title**: `30-Day Service Availability`
* **PromQL Query**:
  ```promql
  avg_over_time(probe_success{job="nemologic-api-health"}[30d]) * 100
  ```
* **Options**: Unit 단위를 `Percent (0-100)`로 지정하고 Min: 0, Max: 100 설정.

### 패널 3: 누적 장애 횟수 (Stat Panel)
* **Title**: `30-Day Incident Count`
* **PromQL Query**:
  ```promql
  changes(probe_success{job="nemologic-api-health"}[30d]) / 2
  ```

### 패널 4: 평균 복구 시간 (MTTR - Stat Panel)
* **Title**: `Mean Time To Recovery (MTTR)`
* **PromQL Query**:
  ```promql
  ((count_over_time(probe_success{job="nemologic-api-health"}[30d]) - sum_over_time(probe_success{job="nemologic-api-health"}[30d])) * 60) 
  / 
  clamp_min(changes(probe_success{job="nemologic-api-health"}[30d]) / 2, 1)
  ```
* **Options**: Unit 단위를 `Duration (seconds)` 또는 `Time > Seconds (s)`로 설정.

### 패널 5: 평균 고장 간격 (MTBF - Stat Panel)
* **Title**: `Mean Time Between Failures (MTBF)`
* **PromQL Query**:
  ```promql
  (sum_over_time(probe_success{job="nemologic-api-health"}[30d]) * 60) 
  / 
  clamp_min(changes(probe_success{job="nemologic-api-health"}[30d]) / 2, 1)
  ```
* **Options**: Unit 단위를 `Duration (seconds)`으로 지정.

---

## 4. Terraform 연동 환경 변수 수집 가이드

IaC(`infra/terraform/grafana.tf`)를 통해 모니터링 체크 및 경보 규칙을 관리하기 위해 필요한 4가지 인증 변수값의 수집 경로입니다.

### ① `grafana_url`
* **의미**: Grafana Cloud 인스턴스 접속용 루트 도메인 주소.
* **수집 경로**:
  1. Grafana Cloud에 로그인한 후, 메인 대시보드 화면으로 이동합니다.
  2. 브라우저 주소창의 URL을 확인합니다.
  3. `https://<your-org-name>.grafana.net` 형식의 주소를 그대로 복사합니다.

### ② `grafana_auth` (Service Account Token)
* **의미**: Terraform이 Grafana 내부에 폴더를 만들고 알림 규칙(Rule Group)을 등록할 수 있게 해주는 API 인증 키.
* **수집 경로**:
  1. Grafana 콘솔 좌측 메뉴에서 **Administration > Users and access > Service accounts**로 이동합니다.
  2. **Add service account** 버튼을 클릭합니다.
  3. **Display name**: `terraform-monitoring-sa` 기입
  4. **Role**: **Editor** 또는 **Admin** 지정 후 **Create** 클릭
  5. 생성된 SA 상세 페이지에서 **Add service account token**을 클릭합니다.
  6. 토큰 이름을 입력하고 **Generate token**을 클릭합니다.
  7. **최초 1회만 화면에 노출되는 토큰 문자열(예: `glsa_...`)을 복사하여 안전한 곳에 보관합니다.**

### ③ `grafana_sm_url` (Synthetic Monitoring API URL)
* **의미**: Grafana Cloud의 전 세계 프로브 체크 엔진을 관리하는 별도의 API 엔드포인트 주소.
* **수집 경로**:
  1. Grafana 콘솔 좌측 메뉴에서 **Testing & synthetics > Synthetics**로 이동합니다.
  2. Synthetics 화면 우측 상단의 **Config** 또는 **Access** 탭을 클릭합니다.
  3. **Synthetic Monitoring API URL** 항목에 명시된 URL 주소를 복사합니다.
     * 예시 (아시아 리전 기준): `https://synthetic-monitoring-api-ap-northeast-1.grafana.net`

### ④ `grafana_sm_token` (Synthetic Monitoring Access Token)
* **의미**: Synthetic Monitoring API에 직접 명령을 내려 HTTP Check 리소스를 생성하고 활성화할 수 있는 전용 액세스 토큰.
* **수집 경로**:
  1. **Testing & synthetics > Synthetics > Config/Access** 화면으로 이동합니다 (위 ③번과 동일 경로).
  2. **API Keys** 또는 **Access Tokens** 항목을 찾습니다.
  3. **Generate Token** 또는 **Create API Key** 버튼을 클릭합니다.
  4. 토큰을 생성한 뒤 화면에 노출되는 **긴 Access Token 값**을 복사합니다.

