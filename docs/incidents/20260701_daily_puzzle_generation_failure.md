# 장애 보고서 (Incident Report) - 2026-07-01

## 1. 장애 개요 (Summary)
* **장애명**: 30x30 대형 퍼즐 생성 시 AI 클라이언트 파싱 오류로 인한 일 배치 스케줄러 중단
* **발생 일시**: 2026년 7월 1일 13:19 KST (04:19 UTC)
* **복구 일시**: 2026년 7월 1일 13:28 KST (04:28 UTC)
* **소요 시간**: 9분
* **영향 범위**: 생산망(Production) 서버 내 30x30 퍼즐 생성 스케줄러

---

## 2. 장애 인지 및 타임라인 (Timeline)
* **13:20 KST**: CloudWatch Alarm `nemologic-server-error-alarm` 경보 발송 (SNS 이메일 수신)
* **13:26 KST**: 개발자 로그 분석 시작 (`aws logs filter-log-events` 수행)
  * 오류 메세지 확인: `Unrecognized token 'Array': was expecting ...` (DailyPuzzleScheduler 예외 트리거)
* **13:27 KST**: 원인 분석 완료 (30x30 그리드 생성 시 Gemini-3.1-flash-lite 모델이 데이터를 축약하기 위해 JS `Array` 코드를 반환하여 JSON 파서 충돌)
* **13:28 KST**: 프롬프트 수정 및 25x25 이상 대형 그리드의 생성 후보 수 축소(5개 -> 2개) 로직 배포, 빌드 및 테스트 확인 완료

---

## 3. 원인 분석 (Root Cause Analysis)
* **AI 출력값 정합성 붕괴**:
  * 30x30 노노그램 그리드는 총 900개의 개별 셀 값을 명시해야 하므로 JSON 문자열이 거대해집니다.
  * 초경량 모델인 `gemini-3.1-flash-lite`가 출력 길이를 줄이고 응답 속도를 높이기 위해, 리터럴 2D 배열 대신 `Array(30).fill(0)` 등 JavaScript의 배열 초기화 코드를 반환했습니다.
  * 백엔드 Jackson `ObjectMapper`가 이를 파싱하는 과정에서 `'Array'`라는 비표준 JSON 토큰을 발견하고 `JsonParseException`을 던졌습니다.
  * 5회 연속 시도가 실패하면서 `DailyPuzzleScheduler`가 ERROR 레벨 로그를 기록하여 CloudWatch 경보가 트리거되었습니다.

---

## 4. 해결 조치 및 재발 방지 대책 (Resolution & Prevention)
* **AI 프롬프트 제약 조건 강화**:
  * AI가 그리드 출력 시 어떠한 형태의 생략 표현식, 루프 함수, 혹은 프로그래밍 코드(예: `Array(30)`, `fill` 등)도 사용하지 못하도록 프롬프트에 `MUST be a literal 2D JSON array` 제약과 구체적인 예외 상황 금지 문구를 강제했습니다.
* **대형 그리드 후보군(Candidate) 축소 정책 도입**:
  * 가로/세로 25개 이상의 대형 퍼즐 생성 시, 토큰 소비와 AI의 연산 부하를 제어하기 위해 후보군 수를 5개에서 2개로 조정했습니다. 이로써 응답 속도 향상 및 출력 단절 현상을 예방했습니다.
