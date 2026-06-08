# Progress State: 네모로직 코어 엔진 개발 진행

## 1. 현재 진행 상황 (Current Progress)

### 디렉토리 구조 및 기본 프레임워크 세팅
- 프로젝트 최상단 디렉토리 구조 (`frontend/`, `backend/`, `infra/`, `docs/`) 구성 완료.
- `frontend/` 디렉토리에 **Vite + Vue.js + TypeScript** 환경 및 **Vitest** 유닛 테스트 러너 환경 설정 완료.

### 힌트 계산 모듈 (Hint Calculator) - TDD Green 단계 완료
- **비즈니스 로직 구현**: `frontend/src/engine/hintCalculator.ts`에 1차원 라인 및 2차원 격자판 힌트 추출 알고리즘 구현 완료.
- **예외 및 입력값 검증**: grid가 null, undefined이거나 빈 배열(`[]`)일 경우 `{ rowHints: [], colHints: [] }`를 안전하게 반환하고, 행들의 길이가 서로 일치하지 않는 불완전한(Inconsistent) 격자일 때만 에러를 던지도록 완화 및 검증 로직 보완 완료.
- **단위 테스트 통과**: `npm run test` 실행 결과, 조건부 예외 완화가 적용된 13개의 모든 테스트 케이스를 100% 통과(Pass)함 확인 완료.

### HTML5 기반 웹 UI 렌더링 및 이벤트 연동 (NonogramCanvas) - TDD Green 단계 완료
- **비즈니스 및 상태 로직 구현**:
  - `frontend/src/engine/puzzleBoard.ts`: 퍼즐 상태(정답, 플레이어 작성용 그리드, 가로/세로 힌트) 데이터 및 셀 변경 액션(`toggleFill`, `toggleMark`), 클리어 검증(`isSolved`) 비즈니스 로직 연동 완료.
  - `frontend/src/engine/coordinateMapper.ts`: 클릭 좌표를 순수 격자 내부 좌표 `(row, col)` 인덱스로 변환하는 수학적 바인딩 로직 구현 완료.
  - `frontend/src/components/NonogramCanvas.vue`: Canvas 2D Context API를 호출해 격자 배경, 힌트 수치, 채움 상태 및 X 기호를 직접 드로잉하며 캔버스 마우스 클릭 이벤트를 `PuzzleBoard` 코어 상태와 연동 완료.
- **단위 테스트 통과**: `npm run test` 실행 결과, 컴포넌트 마운트 및 픽셀 좌표 맵핑 로직을 검증하는 4개의 테스트를 포함한 총 17개의 모든 테스트 케이스를 100% 통과(Pass)함 확인 완료.
- **수동 브라우저 검증 완료**: 5x5 하트 정답 격자판을 기반으로 로컬 개발 서버(`npm run dev`) 환경에서 실제 마우스 좌클릭(채움), 우클릭(X 마크) 입력 시 오프셋 오차 없이 정상 작동함 확인 및 게임 클리어 축하 배너 노출 검증 완료.

### 스프링 부트 백엔드 API 서버 구축 (Stage API) - TDD Red 단계 완료
- **프로젝트 초기화**: `backend/` 디렉토리에 **Java 17 + Spring Boot 3.3.0 + Gradle 8.13** 환경의 백엔드 빌드 구성(`build.gradle`, `settings.gradle`) 완료.
- **스켈레톤 구조 작성**:
  - `com.devdoyen.nemologic.NemologicApplication`: 메인 엔트리포인트 어플리케이션 선언.
  - `com.devdoyen.nemologic.model.Stage`: 가로, 세로 크기 및 정답 격자판을 관리할 레코드(`Record`) 구조체 설계.
  - `com.devdoyen.nemologic.service.StageService`: 스테이지 조회 기능을 담당하는 목업 스텁 클래스 구성.
  - `com.devdoyen.nemologic.controller.StageController`: `/api/stages` 및 `/api/stages/{id}` 엔드포인트를 노출하는 REST 컨트롤러 선언.
- **JUnit 5 MockMvc 테스트 작성**: `StageControllerTest.java`에 전체 목록 조회, 개별 상세 조회 및 예외 처리를 검증하는 스프링 부트 테스트 세트 작성 완료.
- **테스트 실행**: `gradle test` 실행 결과, 목록 조회 및 상세 조회 2개 케이스가 스텁 데이터 미배치로 인해 예상대로 실패(Fail, Red Phase)함을 확인 완료.

---

## 2. 다음 단계: 백엔드 API 및 프론트엔드 개발 (Next Goals)

### 핵심 작업 목록

1. **Java / Spring Boot 백엔드 API 서버 구현 (TDD Green 단계)**
   - `StageService.java`에 하트(5x5) 및 체커보드(10x10) 정답 격자판 데이터를 정적으로 적재하고, 비즈니스 로직을 작성하여 JUnit 테스트 3개를 모두 통과시킵니다.

2. **배열 조작 모듈 (Array Manipulation) 개발**
   - **TDD 진행**: 격자판 드래그 조작(연속 채우기/지우기) 및 다중 셀 상태 토글 로직 검증용 단위 테스트를 먼저 작성합니다.
   - 드래그 동작 시 마우스 버튼 종류(좌클릭/우클릭)에 따른 상태 전이 로직 및 이벤트 리스너 리팩토링.

3. **검증 모듈 (Validator) 개발**
   - **TDD 진행**: 사용자가 풀고 있는 격자판의 실시간 상태가 정답 힌트 정보와 완전히 일치하는지 비교하는 테스트를 먼저 작성합니다.

---

## 3. 개발 규칙 및 제약사항 준수 확인
- **TDD 필수 준수**: 코어 모듈 및 비즈니스 로직 작성 시 반드시 테스트 코드가 선행되어 통과 여부를 검증해야 합니다.
- **디렉토리 격리**: 단일 작업 단위에서는 메인 디렉토리(`frontend/` 또는 `backend/`)를 독립적으로만 수정하며, 여러 메인 디렉토리의 파일들을 동시에 혼재해 수정하지 않도록 통제합니다.
- **반응성 디커플링**: 코어 비즈니스 로직 모듈 내에는 Vue 프레임워크나 반응성 종속성(Ref, Reactive)을 절대 사용하지 않습니다.
