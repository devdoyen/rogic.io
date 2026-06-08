# Progress State: 네모로직 코어 엔진 개발 진행

## 1. 현재 진행 상황 (Current Progress)

### 디렉토리 구조 및 기본 프레임워크 세팅
- 프로젝트 최상단 디렉토리 구조 (`frontend/`, `backend/`, `infra/`, `docs/`) 구성 완료.
- `frontend/` 디렉토리에 **Vite + Vue.js + TypeScript** 환경 및 **Vitest** 유닛 테스트 러너 환경 설정 완료.

### 힌트 계산 모듈 (Hint Calculator) - TDD Green 단계 완료
- **비즈니스 로직 구현**: `frontend/src/engine/hintCalculator.ts`에 1차원 라인 및 2차원 격자판 힌트 추출 알고리즘 구현 완료.
- **예외 및 입력값 검증**: grid가 null, undefined이거나 빈 배열(`[]`)일 경우 `{ rowHints: [], colHints: [] }`를 안전하게 반환하고, 행들의 길이가 서로 일치하지 않는 불완전한(Inconsistent) 격자일 때만 에러를 던지도록 완화 및 검증 로직 보완 완료.
- **단위 테스트 통과**: `npm run test` 실행 결과, 조건부 예외 완화가 적용된 13개의 모든 테스트 케이스를 100% 통과(Pass)함 확인 완료.

### HTML5 기반 웹 UI 렌더링 및 이벤트 연동 (NonogramCanvas) - TDD Red 단계 완료
- **테스트 및 DOM 환경 구축**: 프론트엔드 내에 `@vue/test-utils` 및 `happy-dom`을 의존성으로 추가하고, `vite.config.ts`를 통해 Vitest의 테스트 환경을 `happy-dom`으로 변경 완료.
- **컴포넌트 및 핵심 상태 모델 설계**:
  - `frontend/src/engine/puzzleBoard.ts`: 반응성 시스템과 격리된 퍼즐 정답, 현재 보드 및 힌트 상태 관리 객체(`PuzzleBoard`) 설계 및 스텁 작성 완료.
  - `frontend/src/engine/coordinateMapper.ts`: 클릭 좌표를 격자 셀 인덱스로 변환하는 수학적 연산 로직(`getGridCoordinates`) 선언 및 스텁 작성 완료.
  - `frontend/src/components/NonogramCanvas.vue`: 순수 HTML5 `<canvas>`를 사용해 격자판을 그릴 Vue 컴포넌트 뼈대 작성 완료.
- **단위 테스트 작성**: `frontend/src/components/NonogramCanvas.test.ts`에 컴포넌트 마운트 검증 및 좌표 연산 로직에 대한 테스트 케이스 4개 추가 완료.
- **테스트 실행**: 마운트는 성공하나 좌표 맵핑 테스트가 예상대로 실패(Fail, Red Phase)함을 검증 완료.

---

## 2. 다음 단계: 네모로직 코어 엔진 및 UI 개발 (Next Goals)

### 핵심 작업 목록

1. **NonogramCanvas 및 CoordinateMapper 구현 (TDD Green 단계)**
   - `coordinateMapper.ts`의 좌표 변환 알고리즘 완성하여 단위 테스트 통과 시키기.
   - `NonogramCanvas.vue`의 HTML5 `<canvas>` 그리기 로직을 구현하여 힌트 텍스트 및 격자판 시각화 완료하기.
   - 마우스 이벤트를 바인딩하고 `PuzzleBoard`의 셀 토글 메소드 연동하기.

2. **배열 조작 모듈 (Array Manipulation) 개발**
   - **TDD 진행**: 격자판 상태 데이터 모델(빈 칸: 0, 채움: 1, X 표시: 2)에 대한 조작 및 행/열 업데이트 로직 검증용 단위 테스트를 먼저 작성합니다.
   - 단일 셀 클릭/드래그 동작 시 마우스 버튼 종류에 따른 상태 토글 기능 설계.

3. **검증 모듈 (Validator) 개발**
   - **TDD 진행**: 사용자가 풀고 있는 격자판의 실시간 상태가 정답 힌트 정보와 완전히 일치하는지 비교하는 테스트를 먼저 작성합니다.

---

## 3. 개발 규칙 및 제약사항 준수 확인
- **TDD 필수 준수**: 코어 모듈 작성 시 반드시 테스트 코드가 선행되어 통과 여부를 검증해야 합니다.
- **디렉토리 격리**: 변경 사항은 항상 `frontend/` 디렉토리 내에만 머무르도록 엄격히 제어합니다.
- **반응성 디커플링**: 코어 비즈니스 로직 모듈 내에는 Vue 프레임워크나 반응성 종속성(Ref, Reactive)을 절대 사용하지 않습니다.
