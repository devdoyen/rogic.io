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
   * **Target URL**: `https://rogic.io/api/stages`
   * **Frequency**: `60s` (무료 쿼터 내 가동을 위해 60초 주기 권장)
   * **Timeout**: `5s`
5. **프로브 위치(Probes) 선택**:
   * 아시아권 최적 측정을 위해 **Tokyo (ap-northeast-1)**, **Singapore (ap-southeast-1)**, **Seoul (ap-northeast-2, 지원 시)** 등을 포함하여 전 세계 3개 이상의 Probes를 활성화합니다.
6. **Save**: 설정 완료 후 **Save check**를 클릭합니다.

---

## 2. 이메일 수신처 및 알림 정책 연동 (Alerting)

서버 다운 시 지정된 이메일 주소로 긴급 장애 보고를 받기 위한 알림 채널을 설정합니다.

### ① 연락처(Contact Point) 정의
1. Grafana 메뉴에서 **Alerting > Contact points**로 이동합니다.
2. **Add contact point** 버튼을 클릭합니다.
3. **Name**: `Developer-Email-Alerts`
4. **Integration**: **Email** 선택
5. **Addresses**: 알림을 수신할 이메일 주소 기입 (여러 개일 경우 `,` 또는 `줄바꿈`으로 구분)
6. **Test**: **Test** 버튼을 클릭하여 테스트 이메일이 수신함으로 도착하는지 확인 후 **Save contact point**를 클릭합니다.

### ② 경보 규칙(Alert Rule) 작성
1. **Alerting > Alert rules**로 이동하여 **Create alert rule** 버튼을 클릭합니다.
2. **Rule name**: `Nemologic-Service-Down-Alert`
3. **Query & Expression**:
   * **Query (A)**: Prometheus 데이터 소스를 선택하고 아래 PromQL을 입력합니다.
     ```promql
     sum(sm_check_status{job="nemologic-api-health"})
     ```
     *(설명: 활성화된 프로브들 중 정상인 개수의 합. 모두 다운되면 0이 됨)*
   * **Expression (B) - Threshold**:
     * **Input**: `A`
     * **Condition**: **IS BELOW** `1` (프로브 성공 개수가 1 미만, 즉 전체 실패 시)
4. **Set Alert State**:
   * 일시적인 네트워크 순전(Network blip)에 의한 오경보를 막기 위해 **For** 기간을 `2m` (2분)으로 설정합니다. (2분 연속 실패 시 실제 Alert 상태 돌입)
5. **Configure notifications**:
   * 알림 식별을 위한 레이블 추가: `severity=critical`
6. **Save**: 우측 상단의 **Save rule and exit**를 클릭합니다.

### ③ 알림 라우팅 정책(Notification Policy) 매핑
1. **Alerting > Notification policies**로 이동합니다.
2. **Specific routing policies** 아래에 있는 **New policy** 버튼을 클릭합니다.
3. **Matching labels**: `severity` = `critical` 입력
4. **Contact point**: 앞서 생성한 `Developer-Email-Alerts` 선택
5. **Save policy**를 클릭하여 경보 발생 시 이메일로 알림이 라우트되도록 설정을 완료합니다.

---

## 3. SLA & 가용성 대시보드 구축 (PromQL)

수집된 Synthetic Monitoring 메트릭을 기반으로 한눈에 장애 품질 지표를 볼 수 있는 Grafana 대시보드를 생성합니다.

1. **Dashboards > New dashboard > Add visualization**을 선택합니다.
2. 데이터소스를 **Grafana Cloud Prometheus**로 선택하고 아래 패널들을 구성합니다.

### 패널 1: 실시간 가동 여부 (Stat Panel)
* **Title**: `API Health Status`
* **PromQL Query**:
  ```promql
  sum(sm_check_status{job="nemologic-api-health"})
  ```
* **Options**: Stat 패널 선택, Thresholds 지정 (0: Red, 1 이상: Green).

### 패널 2: 가용성 백분율 (Gauge/Stat Panel - Uptime SLA)
* **Title**: `30-Day Service Availability`
* **PromQL Query**:
  ```promql
  avg_over_time(sm_check_status{job="nemologic-api-health"}[30d]) * 100
  ```
* **Options**: Unit 단위를 `Percent (0-100)`로 지정하고 Min: 0, Max: 100 설정.

### 패널 3: 누적 장애 횟수 (Stat Panel)
* **Title**: `30-Day Incident Count`
* **PromQL Query**:
  ```promql
  changes(sm_check_status{job="nemologic-api-health"}[30d]) / 2
  ```

### 패널 4: 평균 복구 시간 (MTTR - Stat Panel)
* **Title**: `Mean Time To Recovery (MTTR)`
* **PromQL Query**:
  ```promql
  (sum_over_time((1 - sm_check_status{job="nemologic-api-health"})[30d]) * 60) 
  / 
  clamp_min(changes(sm_check_status{job="nemologic-api-health"}[30d]) / 2, 1)
  ```
* **Options**: Unit 단위를 `Duration (seconds)` 또는 `Time > Seconds (s)`로 설정.

### 패널 5: 평균 고장 간격 (MTBF - Stat Panel)
* **Title**: `Mean Time Between Failures (MTBF)`
* **PromQL Query**:
  ```promql
  ((count_over_time(sm_check_status{job="nemologic-api-health"}[30d]) * 60) - (sum_over_time((1 - sm_check_status{job="nemologic-api-health"})[30d]) * 60)) 
  / 
  clamp_min(changes(sm_check_status{job="nemologic-api-health"}[30d]) / 2, 1)
  ```
* **Options**: Unit 단위를 `Duration (seconds)`으로 지정.
