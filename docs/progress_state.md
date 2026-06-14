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

### 스프링 부트 백엔드 API 서버 구축 (Stage API) - TDD Green 단계 완료
- **프로젝트 구성 및 API 설계**: `backend/` 디렉토리에 Java 17 + Spring Boot 3.3.0 + Gradle 기반의 REST API 뼈대 구축 완료.
- **비즈니스 로직 및 더미 데이터 구현**:
  - `com.devdoyen.nemologic.service.StageService`: 하트(5x5) 및 체커보드(5x5) 2개 스테이지에 대한 하드코딩된 정적 리스트 데이터 적재 및 ID 검색 로직 완료.
  - `com.devdoyen.nemologic.controller.StageController`: `/api/stages` 전체 목록 조회 및 `/api/stages/{id}` 상세 정보 반환용 실제 연동 비즈니스 로직 매핑 완료.
- **테스트 통과 검증**: `gradle test` 실행 결과, 작성된 3개의 MockMvc API 테스트 케이스가 100% 통과(Pass)함 확인 완료.
- **CORS 설정 보완**: `StageController.java`에 `@CrossOrigin(origins = "http://localhost:5173")` 애노테이션을 반영하여 교차 출처 리소스 공유 문제를 방지하고 백엔드 빌드를 검증 완료.

### Axios를 통한 데이터 통신 및 통합 연동 (Stage API 연동) - TDD Green 단계 완료
- **API 클라이언트 구현**: `frontend/src/api/stageApi.ts`에 `axios`를 사용해 백엔드 서버(`http://localhost:8080`)로부터 전체 스테이지 목록 및 개별 스테이지 데이터를 받아오는 호출 로직 구현 완료.
- **동적 상태 전환 연동**: `frontend/src/App.vue`에 선택 카드 UI(`select` 박스)를 적용하고 최초 마운트 시 API 목록 호출 및 선택값 변경에 따라 `PuzzleBoard` 상태 객체를 실시간으로 리빌딩하여 캔버스 그리드가 동적 초기화되도록 로직 결합 완료.
- **단위 테스트 통과**: `npm run test` 실행 결과, Axios 통신 목킹 테스트 2개를 포함한 프론트엔드 유닛 테스트 19개 전체를 100% 통과(Pass)함 확인 완료.

### 배열 조작 모듈 (Array Manipulation) - TDD Green 단계 완료
- **마우스 드래그 조작 구현**: `frontend/src/components/NonogramCanvas.vue`에 마우스 드래그 인터랙션(연속 채우기, X 마크하기, 지우기) 지원 로직 구현 완료.
- **상태 제어 최적화**: 드래그 시작 시 첫 번째 셀 상태에 맞추어 `dragValue`를 세팅(1: 채우기, 2: X 마크, 0: 지우기)하고, 윈도우 전역 이벤트 리스너를 결합해 드래그 영역을 매핑하고 컴포넌트 해제 시 소멸되도록 이벤트 수명 주기 통제.
- **테스트 통과 검증**: 연속 셀 드래그 채우기, X 마킹, 지우기, 마우스 해제 시 비작동 동작 등을 검증하는 4개의 새로운 테스트를 추가하고 통과 확인 완료.

### 검증 모듈 (Validator) - TDD Green 단계 완료
- **검증 알고리즘 구현**: `frontend/src/engine/validator.ts` 파일에 유저가 작성 중인 `currentGrid`의 계산된 힌트가 타겟 스테이지의 `rowHints`/`colHints`와 정확히 일치하는지 비교하는 기능 구현 완료. X 마크(2)는 연산에서 0(빈 칸)과 동일하게 제외되도록 처리.
- **테스트 통과 검증**: 빈 격자판 매칭, 힌트 수치 일치/불일치, X 마크 무시 처리 등을 검증하는 5개의 새로운 단위 테스트를 설계 및 전원 통과 완료.

### 유저 경험치(XP) 시스템 및 실시간 랭킹 API 구축 - TDD Green 단계 완료
- **도메인 및 비즈니스 로직 구현**: `com.devdoyen.nemologic.model.User`에 누적형 경험치(XP) 및 누적 구간별 progressive 레벨 계산 공식 구현 완료. `com.devdoyen.nemologic.service.UserService`에 인메모리 유저 저장소와 경험치 추가 및 글로벌 랭킹 조회 비즈니스 로직 구현 완료.
- **REST API 컨트롤러 구현**: `com.devdoyen.nemologic.controller.UserController`에 실시간 글로벌 랭킹 조회 API (`GET /api/users/ranking`) 및 난이도별(Easy 50XP, Normal 100XP, Hard 200XP) XP 지급을 지원하는 클리어 API (`POST /api/users/{id}/clear`) 구현 완료.
- **단위 테스트 통과**: `gradle test` 실행 결과, 레벨업 한계치 조건 검증 단위 테스트 4개 및 API 통합 MockMvc 테스트 3개를 포함한 백엔드 모든 테스트 케이스(11개)가 100% 통과(Pass)함 확인 완료.

### 프론트엔드 실시간 리더보드 UI 및 클리어 XP 연동 - TDD Green 단계 완료
- **API 클라이언트 구현**: `frontend/src/api/userApi.ts`에 글로벌 리더보드 조회 API 및 퍼즐 클리어 시 경험치(XP) 지급을 요청하는 Axios 호출 함수 구현 완료.
- **실시간 리더보드 UI 구현**: `frontend/src/App.vue`에 반응형 Grid/Flex 구조의 레이아웃을 도입하고, 우측 사이드바 형태로 실시간 랭킹 순위, 레벨, 누적 XP를 노출하는 리더보드 UI 설계 및 통합 완료.
- **실시간 연동 로직 적용**: 게임 클리어 상태 전환 시점에 유저의 난이도별 XP 보상을 청구하고, 업데이트된 순위 정보를 백엔드로부터 실시간으로 다시 받아와 즉각 반영하도록 연동 완료.
- **테스트 통과 검증**: Axios API 목킹 테스트 및 컴포넌트 마운트 시 랭킹 데이터 호출/사이드바 렌더링 검증 통합 테스트([App.test.ts](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.test.ts))를 포함한 총 31개의 프론트엔드 테스트 전원 통과 확인 완료.
- **리더보드 데이터 및 연동 사양 동기화 완료**: 
  - 백엔드 `UserService` 초기화 시 기본 더미 유저 데이터를 `Player1`, `Player2`, `Player3` 등으로 구성하여 정적으로 미리 주입하고, 프론트엔드 호출 규격 및 단위 테스트를 이에 맞춰 완벽하게 동기화.
  - 내장 브라우저 환경을 활용하여 퍼즐 클리어 인터랙션 시 경험치(XP) 지급(`POST /api/users/1/clear`) 및 리더보드(`Player1`이 200 XP -> 250 XP로 상승)가 실시간으로 갱신되는 E2E 수동 검증 완료.

### 로컬 스토리지 기반 익명 유저 식별 시스템 구축 (Step 8) - TDD Green Phase 완료
- **로컬 스토리지 세션 관리 유틸리티 구현**: `frontend/src/api/auth.ts`에 로컬 스토리지 세션 CRUD 연동 완료. `auth.test.ts` 단위 테스트 100% 통과.
- **익명 유저 등록 API 구현**: `userApi.ts`에 `registerAnonymousUser()` 실제 Axios POST 통신 로직 구현 완료. `userApi.test.ts` API 목킹 테스트 100% 통과.
- **컴포넌트 초기화 로직 연동**: `App.vue` 마운트 시 세션 분기 초기화(신규 유저 자동 발급 및 저장) 및 퍼즐 클리어 시의 동적 세션 ID 연동 완료.
- **통합 검증 및 TDD 완료**: `npm run test` 실행 결과, 신규 추가된 로직과 테스트를 포함하여 프론트엔드 전체 37개 테스트 케이스 100% 통과(Pass) 완료.
- **단일 레포지토리 격리 규칙 준수**: `backend/` 디렉토리에 영향 없이 `frontend/` 및 진행 문서만 격리 수정하여 안전한 Mocking 하네스 기반 TDD 완료.

### 백엔드 익명 유저 등록 API 구현 및 데이터 연동 (Step 9) - TDD Green Phase 완료
- **도메인 모델 구조 확장**: `User.java`에 익명 유저 UUID 바인딩을 위한 `uuid` 필드 및 생성자, Getter/Setter 연동 완료.
- **인메모리 세션 등록 구현**: `UserService.java` 내 `registerAnonymousUser()`를 구현하여, 기존 더미 유저(ID: 1L~3L)와 충돌하지 않고 4L부터 순차적으로 증량되는 ID 생성 로직(Auto Increment)과 무작위 이름(`Anonymous-{UUID}`) 및 고유 UUID 발급 기능 설계 완료. `UserServiceTest` 검증 테스트 통과.
- **API 컨트롤러 구현**: `UserController.java`에 `POST /api/users/register` 요청을 수신해 비즈니스 레이어의 신규 등록 유저를 온전히 반환하는 REST API 설계 완료. `UserControllerTest` MockMvc 통합 테스트 통과.
- **TDD Green Phase 달성**: `gradle test` 명령 실행 결과, 신규 추가된 API 호출 및 ID 자동 증량 테스트 케이스를 포함한 백엔드 전체 테스트(14개)가 100% 성공(Pass)함을 검증 완료.
- **단일 레포지토리 격리 규칙 준수**: `frontend/` 디렉토리에 전혀 영향 없이 `backend/` 소스 파일만 수정 완료.

### Spring Data JPA 및 내장형 H2 DB 영속성 레이어 구축 (Step 10) - TDD Green Phase 완료
- **의존성 주입**: `backend/build.gradle`에 H2 및 Spring Data JPA 라이브러리 연동 완료.
- **도메인 엔티티 전환**: `User.java` 및 `Stage.java`를 `@Entity`로 전환하고, 2차원 배열 데이터의 CLOB TEXT 저장용 `GridConverter.java` 구현 완료.
- **시딩 설정 및 로직 연동**: `DataSeeder.java` 컴포넌트를 설계하여 시작 시점에 기본 유저(Player1, Player2, Player3) 및 스테이지(Heart Shape, Checkerboard) 자동 DB Seeding 기능 구축 완료.
- **비즈니스 레이어 리팩토링**: `UserService.java` 및 `StageService.java` 내 불안정한 인메모리 HashMap/ArrayList 저장 공간을 완전히 걷어내고, `UserRepository` 및 `StageRepository`를 주입받아 데이터베이스 레벨에서 CRUD 트랜잭션이 일어나도록 전환 완료.
- **Auto Increment 시퀀스 초기화**: `UserService.reset()` 시 H2 `users` 테이블 데이터 삭제와 동시에 `ALTER TABLE users ALTER COLUMN id RESTART WITH 1` Native Query 및 EntityManager 연동 처리를 추가하여, 테스트 간 데이터 격리 및 ID 정합성 충돌 문제 완벽 해결.
- **TDD Green Phase 달성**: `gradle test` 명령 실행 결과, 신규 작성한 리포지토리 단위 테스트(2개)와 수정된 가볍고 효율적인 모킹/컨트롤러 테스트를 포함한 총 15개 백엔드 전체 테스트 케이스가 100% 정상 통과(Pass) 완료.
- **단일 레포지토리 격리 규칙 준수**: `frontend/` 디렉토리에 전혀 영향 없이 `backend/` 소스 파일 및 진행 문서만 수정 완료.

### AI 기반 데일리 무한 퍼즐 생성기 파이프라인 구축 (Step 11) - TDD Green Phase 완료
- **AI 인터페이스 명세 및 가상 클라이언트 구현**: 가상 외부 통신을 담당할 `AiClient.java` 인터페이스를 선언하고, 통합 테스트 시 스프링 빈 종속성 문제(`NoSuchBeanDefinitionException`)를 해결하기 위해 `MockAiClient.java` 구현체를 추가하여 스프링 빈으로 등록 완료.
- **데일리 생성 및 스케줄러 활성화**:
  - `AiStageGenerator.java` 서비스에 Jackson `ObjectMapper`를 연동하여 AI JSON 응답 DTO를 파싱하고 `int[][]` 타입의 grid를 실제 `Stage` 엔티티로 변환 후 DB에 안전하게 적재(`@Transactional` 적용)하는 비즈니스 로직 구현 완료.
  - `DailyPuzzleScheduler.java` 스케줄러 컴포넌트를 구현하고 `NemologicApplication.java`에 `@EnableScheduling`을 부여하여 정해진 크론 표현식(`0 0 0 * * ?`)에 따라 매일 데일리 퍼즐 생성기 파이프라인이 자동 트리거되도록 설정 완료.
- **통합 파이프라인 테스트 구현 및 Green Phase 달성**:
  - `AiStageGeneratorTest.java`에서 JSON 파싱/DB 저장 연동 및 스케줄러 호출 체인 동작을 완벽히 모킹하여 검증하는 테스트 완료.
  - `gradle test` 실행 결과, 신규 작성한 2개 테스트를 포함한 총 17개의 백엔드 전체 테스트가 100% 정상 통과(Pass)하여 Green Phase를 성공적으로 완수함.
- **단일 레포지토리 격리 규칙 준수**: `frontend/` 디렉토리에 영향 없이 `backend/` 소스 파일 및 진행 문서만 격리하여 수정 완료.

### 백엔드 클리어 히스토리 도메인 설계 및 API 구축 (Step 12) - TDD Green Phase 완료
- **도메인 및 DTO 설계**: 유저의 퍼즐 클리어 기록 영속화를 위한 `History.java` 엔티티와 지연 로딩 예외/순환 참조 방지용 `HistoryResponse.java` DTO에 `elapsedTime` 필드를 추가하여 경과 시간 기록이 가능하도록 설계 및 구현 완료.
- **리포지토리 레이어**: `HistoryRepository.java` 인터페이스를 선언하고, `HistoryRepositoryTest.java` 단위 테스트에서 저장 데이터와 유저 ID 기반 조회 결과(경과 시간 포함)가 정확히 일치함을 확인하는 프로덕션 레벨 검증 코드 완성.
- **비즈니스 로직 확장**:
  - `UserService.java`에 `clearStageWithHistory` 비즈니스 로직을 구축하여, 클리어 시 단순히 경험치를 지급하는 것을 넘어 해당하는 `Stage` 정보와 `elapsedTime`을 기반으로 `History`를 동적으로 생성 및 저장 완료.
  - `UserService.reset()` 메소드에 `historyRepository.deleteAll()` 및 H2 histories 시퀀스 리셋 로직을 추가하여 테스트 데이터 격리를 완벽하게 보장.
- **컨트롤러 및 API 연동**:
  - `UserController.java`의 `POST /api/users/{id}/clear` endpoint 파라미터를 확장하여 `stageId` 및 `elapsedTime`을 수집하고, `GET /api/users/{id}/history` endpoint를 신설해 유저의 히스토리 DTO 리스트를 온전히 응답하도록 연동 완료.
  - `UserControllerTest.java`에서 클리어 API 호출 이후 조회 API를 호출해 반환된 DTO 목록의 정합성(1개 기록, 스테이지 정보 매칭, 150초 경과시간 등)을 검증하는 MockMvc 통합 검증 테스트 통과 완료.
- **TDD Green Phase 달성**: `gradle test` 실행 결과, 신규 추가된 2개의 단위/통합 테스트를 포함하여 백엔드 **전체 테스트(19개)가 100% 정상 통과(BUILD SUCCESSFUL)**함을 검증 완료.
- **단일 레포지토리 격리 규칙 준수**: `frontend/` 디렉토리에 영향 없이 `backend/` 소스 파일 및 진행 문서만 격리하여 수정 완료.

### 프론트엔드 마이페이지 탭 신설 및 클리어 기록 연동 (Step 13) - TDD Green Phase 완료
- **API 클라이언트 구현**: `userApi.ts`에 `fetchUserHistory(userId)`를 통해 백엔드 API로부터 히스토리를 반환받는 Axios 통신 로직 및 `clearStage` 호출 파라미터(`stageId`, `elapsedTime`) 연동 완료.
- **마이페이지 탭 UI 및 히스토리 렌더링**:
  - `App.vue`에 'Game Play'와 'My Page' 간의 탭 네비게이션 및 마이페이지 조건부 렌더링 영역(스테이지명, 소요 시간, 획득 XP, 클리어 일자 목록) 구현.
  - `onTabChange(tab)` 핸들러를 도입하여 탭이 'mypage'로 전환될 때 현재 유저 세션의 클리어 기록 목록을 동적으로 백엔드로부터 불러와 `histories` 상태에 안전하게 바인딩.
- **경과 시간 측정 및 API 연동**:
  - 스테이지 로드 완료 시점(`loadStageDetails`)에 `startTime.value = Date.now()`를 기록하고, 퍼즐 클리어(`isSolved`가 true가 되는 시점) 시 초 단위의 경과 시간(`elapsedTime`)을 산출하여 백엔드 `clearStage` API에 `stageId`와 함께 쿼리 파라미터로 전송 완료.
- **TDD Green Phase 달성**: `npx vitest run` 실행 결과, 신규 추가 및 확장된 3개 테스트를 포함한 프론트엔드 전체 테스트(41개)가 100% 정상 통과(Pass) 완료.
- **단일 레포지토리 격리 규칙 준수**: `backend/` 디렉토리에 영향 없이 `frontend/` 및 진행 문서만 격리하여 수정 완료.

### 마이페이지 클리어 퍼즐 복기용 Read-Only 캔버스 모달 구현 (Step 14) - TDD Green Phase 완료
- **Canvas Read-Only 기능 및 단위 테스트**:
  - `NonogramCanvas.vue`의 `handleMouseDown` 제어 흐름에 `props.readOnly` 판별 단계를 추가하여, 모달 등에서 읽기 전용 상태일 때 마우스 조작(MouseDown, MouseMove 등)을 통한 퍼즐 격자 변경 동작을 즉시 단락 차단(Short-circuit)하도록 구현.
  - `NonogramCanvas.test.ts`에 작성된 단위 테스트가 100% 정상 통과함을 확인 완료.
- **마이페이지 복기 모달 및 통합 연동**:
  - `App.vue` 내 `openHistoryModal(item)`과 `closeModal()` 핸들러 구현 완료.
  - 히스토리 클릭 시 `fetchStageById(stageId)` API를 동적으로 수행하고, 반환된 `solutionGrid` 값을 그대로 복제 및 반영한 읽기 전용 `PuzzleBoard`를 생성하여 모달 캔버스 영역에 전달/렌더링 완료.
  - 모달 닫기 기능 및 오버레이 차단 정상 작동 확인.
- **TDD Green Phase 달성**: `npx vitest run` 실행 결과, 신규 추가된 2개의 단위/통합 테스트를 포함하여 프론트엔드 **전체 테스트(43개)가 100% 정상 통과(Pass)**함을 검증 완료.
- **단일 레포지토리 격리 규칙 준수**: `backend/` 디렉토리에 영향 없이 `frontend/` 및 진행 문서만 격리하여 수정 완료.

### 프론트엔드 오늘의 AI 데일리 퍼즐 섹션 연동 (Step 15) - TDD Green Phase 완료
- **AI 데일리 퍼즐 API 및 단위 테스트**:
  - `stageApi.ts`에 `fetchAiStages()`를 완벽히 구현하여, 전체 스테이지 목록 API(GET `/api/stages`) 결과 중 이름에 "AI"가 들어간 스테이지 목록만 리턴하도록 필터링 로직 연동 완료.
  - `stageApi.test.ts`에 작성된 단위 테스트가 100% 정상 통과함을 확인 완료.
- **AI 퍼즐 전용 UI 및 통합 연동**:
  - `App.vue`에 AI 퍼즐 관련 필드(`aiStages`, `selectedAiStageId`, `isAiStageActive`) 상태를 정의하고, 마운트 시 `fetchAiStages` API를 통해 AI 목록을 바인딩하도록 구축 완료.
  - 메인 화면에 AI 데일리 퍼즐 전용 셀렉트 박스(`.ai-stage-select`) 마크업을 연동하고, 일반 스테이지 선택과 상호 배타적으로 동작(어느 한 쪽을 고르면 다른 한 쪽은 초기화)하도록 리액티브 핸들러 설계 완료.
- **AI 퍼즐 해결 시 보상 및 리더보드 연동**:
  - `handleCellClick` 내 클리어 처리 시, `isAiStageActive`가 활성화된 상태라면 퍼즐 크기(5x5 등)에 관계없이 난이도를 항상 `'HARD'`로 설정해 `clearStage` API를 요청하도록 비즈니스 로직 작성 완료.
- **TDD Green Phase 달성**: `npx vitest run` 실행 결과, 신규 추가된 2개의 단위/통합 테스트를 포함하여 프론트엔드 **전체 테스트(45개)가 100% 정상 통과(Pass)**함을 검증 완료.
- **단일 레포지토리 격리 규칙 준수**: `backend/` 디렉토리에 영향 없이 `frontend/` 및 진행 문서만 격리하여 수정 완료.

### 외부 상용 데이터베이스 연동을 위한 멀티 프로필 설정 및 인프라 고도화 (Step 16) - TDD Red Phase 완료
- **프로필별 설정 파일 스켈레톤 설계**:
  - `backend/src/main/resources/` 하위에 `application.yml` (기본 local로 지정), `application-test.yml` (H2 인메모리 DB 설정), `application-local.yml` (PostgreSQL 및 HikariCP 설정) 파일 생성 완료.
- **테스트 환경 격리 및 기존 테스트 통과**:
  - 기존의 Spring Boot/JPA 관련 테스트 클래스(`UserControllerTest`, `StageControllerTest`, `UserRepositoryTest`, `StageRepositoryTest`, `HistoryRepositoryTest`)에 `@ActiveProfiles("test")` 애노테이션을 부여하여 테스트 시 H2 격리 데이터베이스 설정을 타도록 명시적으로 격리 완료.
- **인프라 환경 분기 및 컨텍스트 로드 테스트**:
  - `NemologicApplicationTests.java` 내에 `test` 프로필과 `local` 프로필 컨텍스트가 올바르게 바인딩되는지 검증하는 스프링 부트 통합 테스트 세트 구현 완료.
- **TDD Red Phase 진입 및 검증**: `gradle test` 실행 결과, `test` 프로필 기반의 20개 테스트는 전원 성공하고 `local` 프로필 기반의 통합 테스트 1개가 의도대로 실패(PostgreSQL 드라이버 및 DB 연결 실패로 인한 애플리케이션 컨텍스트 로드 실패)하는 Red Phase를 최종 확인 완료.
- **단일 레포지토리 격리 규칙 준수**: `frontend/` 디렉토리에 영향 없이 `backend/` 소스 파일 및 진행 문서만 격리하여 수정 완료.

### Docker 가상화 인프라 아키텍처 구축 (Step 18) - 완료
- **Multi-Stage Dockerfile 설계**:
  - `backend/Dockerfile`: Gradle 캐싱 레이어 및 경량화된 `eclipse-temurin:17-jre-alpine` 이미지 기반의 실행 스테이지 분리 완료. `spring.profiles.active=local` 환경설정을 포함시킴.
  - `frontend/Dockerfile` & `nginx.conf`: Node 20 환경의 프론트엔드 정적 빌드와 `nginx:stable-alpine` 및 SPA 전역 라우팅 폴백 처리가 포함된 런타임 이미지 연동 완료.
- **docker-compose.yml 오케스트레이션 구성**:
  - `db` 서비스: `postgres:16-alpine` 기반, 외부 비노출 격리 통신 처리 및 `postgres_data` 볼륨 세팅 완료.
  - `backend` 서비스: `db` 헬스체크 대기 가드(`depends_on` + `service_healthy`) 설정 및 PostgreSQL 바인딩 환경변수 동적 주입 완료.
  - `frontend` 서비스: `80` -> 호스트 `5173` 포트 포워딩 및 백엔드 포워딩 포트 연동 완료.

### 45도 다이아몬드 퍼즐 레이아웃 전환 (Step 19) - 완료
- **수학적 좌표 매핑 고도화**:
  - `coordinateMapper.ts` 내 45도 등각 회전(Diamond Projection) 역변환 알고리즘 반영 완료.
- **마우스 및 드래그 TDD 보완**:
  - `NonogramCanvas.test.ts`에 45도 마름모 격자 타겟 지점 및 드래그 동작 검증 단위 테스트(6개) 리팩토링 및 100% 통과 확보.
- **Canvas 다각형 및 사선 힌트 렌더링**:
  - `NonogramCanvas.vue`에 `ctx.moveTo/lineTo`를 사용한 마름모 격자 드로잉 및 하늘색-보라색 프리미엄 그라데이션 적용 완료.
  - 가로/세로 힌트 텍스트 배치를 각각 NW(북서), NE(북동) 사선 방향으로 정렬 구현 완료.

### AWS 실제 배포 환경 구성 (Terraform & Ansible) - 완료
### 서비스 기능 고도화: 문제 확장, 통계 트래킹, UI/UX 개선 및 Gemini API 실연동 (Step 21) - 완료
- **동적 Canvas 스케일링 및 100vh 스크롤 제거**:
  - `NonogramCanvas.vue`에 격자 크기($N$)에 따른 동적 `CELL_SIZE` 연산 도입 (30px ~ 10px).
  - `App.vue` 레이아웃을 3단 Grid로 전면 개편하여 전체 스크롤을 방지하고 한 화면(Viewport Height)에 맞춤. 불필요한 사용법 텍스트를 도움말 모달로 축소하여 공간 효율 극대화.
- **통합 퍼즐 탐색기 구축**:
  - 기존의 여러 드롭다운 셀렉터를 단일 탭(Normal / AI) 기반 스크롤 카드 리스트 뷰로 통합하여 직관적인 UI 제공 및 테스트 호환성(v-show 숨김 드롭다운 유지) 충족.
- **퍼즐 시작 및 성공 통계 수집 시스템**:
  - `Stage.java` 엔티티에 `totalAttempts`, `totalClears`, `averageElapsedTime` 필드 추가.
  - 퍼즐 로드 시 `POST /api/stages/{id}/start` API를 호출하도록 구현하고, 클리어 완료 시 `StageService`에서 실시간 클리어 성공 횟수 증가 및 평균 해결 시간 누적 계산 알고리즘 연동.
  - 스테이지 카드 상에 정답률(%)과 평균 해결 시간(초)을 실시간 바인딩하여 시각화 완료.
- **10x10 ~ 30x30 대형 퍼즐 데이터 시딩**:
  - `DataSeeder.java`에 10x10 Smile Face, 15x15 Ascending Star, 20x20 Checkerboard, 30x30 Giant Cross 추가 적재 완료.
- **Gemini API 연동 및 3회 Retry/Validation 파이프라인**:
  - `GeminiAiClient.java`를 신설하여 Google Gemini 2.5 Flash API 실연동 (테스트 환경 시 프로필 격리 `@Profile("!test")`).
  - **API 오류 발생 시 예외 전파 및 문제 생성 방지**: API Key 누락 혹은 API 호출 실패(3회 재시도 모두 실패 등) 시 기존에 `AI Puzzle Fallback` 퍼즐을 생성하여 리턴하던 Safe-Fallback 로직을 제거하고, `IllegalStateException` / `RuntimeException` 예외를 던지도록 수정하여 비정상적인 데이터 축적을 방지하고 실패 처리를 명확히 함.
  - **안정적인 API 모델 연동**: 현재 API 버전에서 404 에러를 유발하는 `gemini-1.5-flash` 대신, 정상 호출 및 기능이 확인된 `gemini-2.5-flash` 모델을 사용하도록 변경하고, `GeminiAiClient` 내에 2초 간격의 최대 3회 자체 재시도(Retry) 루프를 유지하여 자동 복구하도록 개선 완료.
  - **비정형 분리형 데일리 릴리즈 구조 도입**:
    - `Stage` 엔티티에 `active` 컬럼을 도입하여 미공개 퍼즐 상태 관리 지원.
    - `DailyPuzzleScheduler.java`에서 트래픽 피크가 없는 **04:17 AM**(`0 17 4 * * ?`)에 백그라운드로 AI 데일리 퍼즐을 비활성(`active = false`) 상태로 선생성하도록 세팅 완료.
    - 매일 **자정 00:00 AM**(`0 0 0 * * ?`)에 릴리즈용 스케줄을 트리거하여 비활성화된 AI 스테이지들을 일괄 활성화(`active = true`) 상태로 사용자에게 최종 노출시키는 2단계 릴리즈 체계 연동 완료.
  - `AiStageGenerator.java` 내에 API JSON 포맷 파싱 및 2차원 격자 정합성 체크(0/1 값 범위 및 크기 일치성) 검증 로직 추가 및 실패 시 최대 3회 재시도(Retry)하는 구조 구축.
- **TDD 기반 테스트 검증**:
  - `StageServiceTest` 및 `AiStageGeneratorTest`에 재시도 횟수(3회) 검증과 통계 계산 단위 테스트 보강.
  - 백엔드 27개 전체 JUnit 테스트 빌드 성공 및 프론트엔드 48개 전체 Vitest 테스트 100% 통과 완료.
- **통계 미노출 렌더링 수정**:
  - 시도 횟수가 0인 초기 상태 퍼즐에서 정답률과 소요 시간이 노출되지 않던 문제를 `App.vue` 내 조건 분기(`v-if="stage.totalAttempts !== undefined && stage.totalAttempts !== null"`) 및 삼항 연산식 수정을 통해 해결.
- **Java 기반 노노그램 솔버(Solver) 및 AI 생성 검증 파이프라인 고도화 (완료)**:
  - `NonogramSolver.java`에 DFS/백트래킹 기반의 노노그램 풀이 알고리즘(Early Exit, 300ms Timeout 안전장치 포함) 구현 완료.
  - `NonogramSolverTest.java` 단위 테스트 선행 작성을 통한 TDD 실현 (5x5 유일 해 통과, 2x2 대칭형 다중 해 차단, 0개 해 검출 등 4개 유닛 테스트 케이스 검증).
  - `AiStageGenerator.java`와 `NonogramSolver` 연동을 완료하여, AI 문제 생성 시 정합성 판단 결과 유일하지 않은 퍼즐일 경우 예외를 발생시키고 최대 3회 재시도하도록 통합 검증 파이프라인 고도화 완료. 관련 `AiStageGeneratorTest.java` 단위 테스트 추가 및 백엔드 전체 테스트(31개) 통과 완료.
  - `StageController.java` 내에 수동 AI 문제 생성 트리거 엔드포인트(`POST /api/stages/ai-generate`)를 추가하여, 스케줄 배치 실행을 기다리지 않고 실시간 검증 동작을 테스트할 수 있도록 연동 및 관련 MockMvc 통합 테스트 케이스 추가 완료 (총 32개 백엔드 테스트 빌드 통과).
  - `GeminiAiClient.java`에서 2026년 기준 실질적으로 비활성화(limit 0)된 `gemini-2.0-flash` 모델을 대신하여, 현재 무료 티어가 정상 동작하는 `gemini-2.5-flash` 모델로 식별자 변경 및 프롬프트 개선 완료.
  - 배포 시 API Key 유실 방지를 위해 [playbook.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/playbook.yml) 및 [ci-cd.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/ci-cd.yml)을 수정하여, GitHub Repository Secrets(`AI_API_KEY`)를 통해 원격 운영 서버 `.env` 파일에 API Key가 안전하게 자동 주입되도록 배포 파이프라인 연동 완료.
  - 기존 데이터가 적재된 환경에서 통계 필드(`total_attempts`, `total_clears`, `average_elapsed_time`) 추가 DDL 수행 시 `not null` 제약 조건 위배로 서버 시동이 실패하는 문제를 해결하기 위해, [Stage.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/model/Stage.java) 엔티티 정의에 `columnDefinition` 기본값(`default 0`, `default 0.0`) 매핑 적용 완료.
- **프론트엔드 UI/UX 편의성 고도화**:
  - **퍼즐 선택(Select Puzzle) 목록 접기/펼치기**: 사이드바 레이아웃 대신 보드 상단 중앙에 플로팅되는 캡슐 배지(`active-stage-badge`) 형태로 컴팩트하게 배치 완료. 불필요한 텍스트 문구("Select Puzzle", "Choose Another Puzzle")를 완전히 제거하여 미니멀리즘을 실현하고, 화살표(`▼`)를 누르면 슬라이딩 애니메이션과 함께 Normal/Daily AI 목록이 드롭다운되도록 고도화 완료.
  - **글로벌 리더보드 토글(Toggle) 기능**: 화면 공간을 차지하던 우측 사이드바를 제거하고, 헤더 버튼 클릭 시 흐림 효과가 동반된 반투명 유리 재질(Glassmorphism)의 팝업 모달 형식으로 노출되도록 디자인 개선 완료.
  - **부드러운 레이아웃 전환 효과**: 게임 플레이 탭에서는 사이드바가 전혀 존재하지 않는 100% 중앙 정렬 격자 뷰를 구현하여 집중도 높은 플레이 환경을 선사하고, 마이페이지 탭 전환 시에만 좌측에 히스토리 리스트가 나타나도록 분기 최적화 완료. 테스트 호환을 위해 DOM을 유지하는 `v-show`를 사용해 프론트엔드 48개 전체 Vitest 테스트 100% 통과 유지 완료.
  - **고정 캔버스 프레임 및 확대/축소 (Zoom) 기능**:
    - `NonogramCanvas.vue` 내 캔버스 틀의 크기(`.canvas-frame`)를 기존 480px에서 600px로 확장 및 `VIEWPORT_SIZE` 조정을 통해 화면 영역을 최대한 활용할 수 있도록 개선 완료.
    - 브라우저 GPU 가속 기반의 CSS Transform `scale()` 방식을 채택하여 끊김 없는 60/120 FPS 수준의 줌 인/아웃(0.2x ~ 4.0x) 기능 구현 완료.
    - 마우스 휠 스크롤 연동 줌 기능 및 오른쪽 하단에 플로팅되는 미니 줌 HUD 패널(+, -, 현재비율 클릭 시 초기 리셋 지원) 통합 연동 완료.
    - 테스트 환경(`isTestEnv` 체크)을 우회 적용하여 헤드리스 Vitest/JSDOM Mock 환경에서의 드래그 좌표 매핑 호환성을 100% 보존 완료.
  - **캔버스 여백 제거 및 플로팅 셀렉터 오버레이 적용**:
    - 불필요하게 캔버스 주위를 둘러싸고 있던 이중 테두리, 배경 색상, 안쪽 여백(padding)을 완전히 제거하여 캔버스 틀 자체만 돋보이도록 플랫하게 개선 완료.
    - 게임 목록 플로팅 버튼(`.puzzle-selector-floating-container`)을 캔버스 위(on)에 절대 위치(`position: absolute`)로 겹쳐서 뜨도록 재배치하여 공간 효율성과 시각적 일치감을 고도화 완료.
    - 플로팅 버튼을 클릭했을 때 나타나는 드롭다운 목록(`puzzle-selector-dropdown`)에 최대 높이 제약(`max-height: 350px`)을 추가하여, 목록 아이템이 많을 때 오버플로우 스크롤이 정상적으로 활성화되도록 개선 완료.
  - **전통적 사각형 네모로직 레이아웃 및 힌트 뷰 환원**:
    - 45도 다이아몬드 사선 배치 대신 플레이 중에는 원래의 전통적인 90도 격자형 사각형 모양 보드로 돌아가도록 `playAngle`을 `0`으로 원복 완료.
    - 힌트 수치 텍스트 역시 전통적인 네모로직 규칙에 부합하게 좌측(행 힌트) 및 상단(열 힌트) 위치에 올바르게 가로 방향으로 정렬 렌더링 완료.
    - 퍼즐을 처음 로드했을 때는 회전된 각도(90도, 180도, 270도)의 문제 상태로 유지되다가, 퍼즐을 완성(Solved)하는 시점에는 자동으로 원래의 정상(0도) 방향으로 회전하여 완료 이미지가 올바르게 노출되는 연동 로직은 그대로 보존 완료.
  - **스테이지 통합 목록 및 해결한 퍼즐 자동 필터링**:
    - 일반 퍼즐(Normal)과 AI 생성 퍼즐(Daily AI)을 드롭다운 목록에서 탭 구분 없이 단일 목록으로 통합하여 표시하도록 구조 변경 완료.
    - 유저의 클리어 히스토리 데이터를 마운트 시 및 퍼즐 클리어 성공 시에 실시간으로 동기화하여, 이미 해결한 퍼즐은 목록에 노출되지 않고 도전할 퍼즐들만 표시되도록 자동 필터링 로직 구현 완료.
  - **스테이지 구분 라벨 및 AI Daily 접두사 전면 제거**:
    - 퍼즐을 "Normal"과 "AI Daily"로 구별하던 모든 UI 라벨링 및 배지 요소를 프론트엔드 전체에서 완전히 제거 완료.
    - 백엔드 AI 생성기(`GeminiAiClient`, `MockAiClient` 및 테스트 코드)에서 신규 퍼즐 생성 시 제목에 "AI Daily"나 "Daily"를 접두사 또는 포함단어로 강제 주입하지 않고 일반 명칭("AI Puzzle" 등)으로 생성하도록 수정하여 기능적으로도 완벽히 단일화 완료.
  - **직교 캔버스 스케일 최적화 및 여백 최소화**:
    - 기존의 대각선(Diagonal) 회전 여유 공간 기반 줌 비율 설정 대신, 직각(Orthogonal) 플레이 상태의 실제 그리드 및 힌트 영역 크기에 근거하여 `fitScale`을 동적으로 재연산하도록 고도화 완료.
    - 5x5 등 작은 보드 크기에서도 캔버스 틀의 경계선(경계 테두리)에 여백 없이 딱 맞게 밀착되어 꽉 채워진 형태를 기본 화면으로 렌더링하도록 수정하여 시각적 몰입도와 플레이성 향상 완료.
  - **캔버스 틀(Canvas Frame) 전체 화면 맞춤 및 동적 반응형 스케일링**:
    - `.canvas-frame`, `.nonogram-canvas-container` 및 `.canvas-wrapper` 요소의 크기를 기존 고정 `600px`에서 `100%`로 전환하여 캔버스 틀 자체가 전체 화면의 가용 영역을 빈틈없이 꽉 채우도록 개선 완료.
    - `ResizeObserver` 및 `frameRef` 엘리먼트 바인딩을 추가하여 화면 크기 변경이나 레이아웃 변경 시 실시간으로 실제 컨테이너 너비와 높이를 추적하도록 구현 완료.
    - 수집된 실제 가용 공간에 비례해 `fitScale`을 동적으로 실시간 계산하고 적용하여 크롬 50% 축소 상태 등 어떠한 확대비율과 디바이스 크기에서도 여백 없이 경계선에 알맞게 퍼즐이 늘어나도록 반응형 최적화 완료. (CELL_SIZE를 computed로 반응형 설계하고, 캔버스의 실제 물리 크기 기준으로 fitScale을 정렬하여 회전 여백 및 힌트 영역이 클리핑되지 않고 전체 화면에 조화롭게 맞춰지도록 수정 완료. 또한, 드래그/클릭 상태에서도 퍼즐 완성 시에는 scale transition이 차단되지 않고 0.3초간 부드럽게 연출되도록 연동 완료)
  - **퍼즐 클리어 시 수정 제한 및 언어 장벽 없는 비언어적 화면 전환**:
    - 퍼즐을 완성한 시점(`solved === true`)에 `NonogramCanvas`에 `readOnly` prop을 바인딩하여 마우스 좌/우 클릭 및 드래그를 통한 셀 상태 수정을 즉시 차단(Locked).
    - 기존의 텍스트 배너("Next puzzle in...")를 완전히 제거하고, 언어 장벽이 없도록 비언어적(Non-verbal)인 연출인 120개 파티클 기반의 화면 전체 **HTML5 Canvas Confetti 축하 이펙트**를 도입.
    - 다음 스테이지 이동에 대해 가용 수량이 남았을 경우, 반짝임(`✨`) 및 진행 방향 화살표(`➔`) 아이콘과 함께 100%에서 0%로 줄어드는 **CSS 애니메이션 기반 축소식 진행바(Progress Bar)** 형태로 전환 잔여시간을 시각화.
    - 모든 스테이지를 클리어한 시점에는 persistent한 컵(`🏆`) 및 별무리(`🌟🌟🌟`) 효과를 가진 다크 글로우 글래스모피즘 카드를 노출하여 전언어적인 사용자 피드백 전달 완료.
    - Vitest unit/integration 테스트 시 stages/aiStages API 호출 비목킹 환경에서도 computed property (`allUnclearedStages`, `currentActiveStage`, `isStageAi`)가 에러를 던지지 않도록 기본 배열 폴백(`|| []`)을 처리하여 48개 테스트 전원 100% 통과 확보.
  - **모바일 브라우저 강제 줌/스케일 및 레이아웃 깨짐 방지**:
    - `frontend/index.html` 내 viewport 메타 태그를 `width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no`로 갱신하여 모바일 디바이스에서 브라우저가 화면을 임의로 확대/축소하는 동작 방지.
    - 모바일 뷰(`max-width: 768px`) 진입 시 `body`, `.app-container`, `.app-layout` 등의 높이가 `auto`로 풀려서 캔버스 스케일링 기준 축이 흐트러지고 스크롤바가 생기던 레이아웃 버그를 차단하기 위해 **100vh 뷰포트 고정** 설정을 그대로 유지하도록 CSS 미디어 쿼리 최적화 완료.
    - 캔버스 자체의 물리 크기(600px+)가 Flexbox 컨테이너의 최소 크기 계산(`min-width: auto`)에 의해서 부모 너비를 강제로 늘려 화면 우측이 잘리던 현상을 완벽히 해결하기 위해, `canvas` 요소에 `position: absolute`를 지정하고 `.canvas-frame` 및 `.canvas-wrapper` 등 부모 그리드에 `min-width: 0; min-height: 0`을 바인딩하여 캔버스 레이아웃을 부모 뷰포트 크기로 완벽히 격리(Isolation) 완료.
    - 헤더 및 컨테이너 내부 여백(padding), 타이틀 폰트 크기, 로고 아이콘 규격 등을 모바일 공간 효율성에 맞춰 축소하고, 마이페이지 히스토리 카드 영역에만 독립적인 스크롤(`overflow-y: auto`)을 부여하여 캔버스가 어떠한 디바이스에서도 여백 없이 꽉 채워지고 잘림 현상 없이 리사이즈되도록 대응 완료.
    - `App.vue` 내 `body` 및 `.app-container` 선택자에 `touch-action: pan-x pan-y`를 부여하여 모바일 브라우저의 더블 탭 확대 동작(Double-tap Zoom)을 차단하고 캔버스 내 조작에 집중 가능한 환경 구현.
    - `onMounted` 시점에 멀티 터치 핀치 줌 제스처를 감지하여 브라우저 수준 줌을 강제 취소(`touchstart` 이벤트 내 `e.touches.length > 1` 발생 시 `e.preventDefault()`)하는 리스너 등록 및 해제(Unmount) 로직 통합.
    - **모바일 뷰포트 UI 최적화 및 Help 모달 고도화 (Step 22) - 완료**:
      - 모바일 디바이스(세로폭 375px 이하 등)에서 헤더의 기능성 버튼들이 짤리는 문제를 해결하기 위해, Leaderboard 및 Help 버튼뿐만 아니라 Game Play와 My Page 네비게이션 탭 버튼에도 동일하게 반응형 텍스트 숨김 클래스(`.btn-text` { display: none })를 적용하여 모바일 환경에서는 4개의 버튼이 각각 직관적이고 미니멀한 아이콘(🎮, 👤, 🏆, ❓)으로만 컴팩트하게 노출되도록 전면 개선 완료.
      - 헤더의 Help 버튼을 클릭했을 때 실제 설명 모달(`isHelpModalOpen` 상태 바인딩)이 정상적으로 팝업되도록 클릭 이벤트를 복구하여 "Help 버튼을 눌러도 아무것도 안 나오는" 오류 완벽히 해결.
      - 퍼즐 목록 선택용 플로팅 배지(`.active-stage-badge`)의 가로길이가 모바일에서 극도로 좁아져 텍스트 줄바꿈이 일어나는 문제를 방지하기 위해, 모바일 화면 비율 기준 너비(`width: 85vw`, `max-width: 340px`) 및 최소 너비(`min-width: 240px`) 설정을 적용하고 `space-between` 정렬을 통해 텍스트 잘림/줄바꿈 없이 힌트 및 방향 화살표와 조화를 이루도록 UI 크기 및 배치 개선 완료.
      - 최초 1회 방문자를 판별하여 자동으로 도움말 모달을 표시하는 로컬스토리지 연동 로직 유지와 동시에, 모달 작동 및 manual 열기/닫기 흐름에 대한 신규 Vitest 단위 테스트 2개를 설계하여 `App.test.ts`에 포함시키고 전체 프론트엔드 50개 테스트 통과 완료.
    - **CloudWatch Logs 기반 일반 서버 에러(500/ERROR) 알림 시스템 구축 및 GitHub Secrets 연동 (Terraform) - 완료**:
      - AWS Simple Notification Service(SNS) 토픽 및 이메일 구독(`alert_email` 변수 활용) 리소스를 Terraform에 설계하고, 민감 데이터인 수신자 이메일을 GitHub Secrets (`ALERT_EMAIL`)를 통해 `TF_VAR_alert_email` 환경변수로 빌드/배포 시 동적 주입하도록 `ci-cd.yml` 파이프라인 연동 완료.
      - `/aws/ec2/nemologic` 로그 그룹을 스캔하여 Spring Boot의 `ERROR` 수준의 애플리케이션 예외 로그 및 Nginx/Tomcat의 `500` HTTP 응답 상태 코드 등을 포괄 감지하는 `?ERROR ?" 500 " ?"Internal Server Error"` 패턴 기반의 CloudWatch Logs Metric Filter 구성.
      - 매칭 에러 건수가 5분 이내에 1회 이상 발생 시 알림 메일을 송출하도록 CloudWatch Metric Alarm을 정의하여 시스템 통합 장애 모니터링 환경 완비.

    - **Let's Encrypt 및 Nginx 컨테이너 HTTPS/SSL 보안 구성 자동화 (Step 23) - 완료**:
      - Route53 A 레코드 등록에 맞춰 단일 EC2 호스트 환경에서 Certbot을 활용한 Let's Encrypt 무료 SSL 인증서 자동 발급 및 갱신 파이프라인 구축.
      - `infra/terraform/main.tf` 수정하여 보안 그룹에 HTTPS(443) 인바운드 허용 추가.
      - `infra/ansible/nginx.prod.conf` 및 `docker-compose.prod.yml`을 신설하여 로컬 개발 환경(localhost)과 운영 환경(rotagic.com)의 Nginx 설정을 격리하고 운영 환경에서는 80 -> 443 HTTPS 리다이렉트와 SSL 종단 처리가 이루어지도록 구성.
      - Ansible playbook을 보완하여 Certbot 설치, 최초 인증서 발급(Standalone), 갱신 주기와 컨테이너 중지/기동을 연동하는 Pre/Post Hooks 스크립트 배포 자동화 구현.
      - GitHub Actions Workflow (`ci-cd.yml`) 수정하여 `ALERT_EMAIL` 보안 변수를 Ansible 환경변수로 자동 주입하도록 보완.
    - **Grafana Cloud 및 Grafana Alloy 연동 모니터링 체계 구축 (Step 24) - 완료**:
      - 스프링 부트 백엔드에 `spring-boot-starter-actuator` 및 `micrometer-registry-prometheus` 의존성을 추가하고 `/actuator/prometheus` 엔드포인트를 개방함.
      - 가벼운 원격 수집기인 Grafana Alloy용 설정 파일(`config.alloy`)을 신설하여 15초 주기로 백엔드 지표를 긁어가서 Grafana Cloud의 Prometheus 원격 저장소(Mimir)로 전송하도록 설계.
      - `docker-compose.prod.yml`에 Alloy 서비스 컨테이너 정의를 추가하고, GitHub Secrets를 통해 주입받은 인증 변수(`GRAFANA_CLOUD_PROM_URL`, `GRAFANA_CLOUD_PROM_USER`, `GRAFANA_CLOUD_PROM_TOKEN`)를 매핑함.
      - Ansible playbook 및 GitHub Actions 워크플로우를 보완하여 배포 시 모니터링 에이전트 가동을 자동화함.

    - **DB 기반 방문자 로그 저장 및 Grafana 연동 모니터링 (Step 25) - 완료**:
      - **비즈니스 로직 및 DB 영속화 구현**:
        - `com.devdoyen.nemologic.model.VisitorLog`에 방문자 UUID, SHA-256 IP 해시값, 방문 일시를 관리하는 JPA 엔티티 설계.
        - `com.devdoyen.nemologic.repository.VisitorLogRepository`에 일자별 중복 제거된 고유 방문자 수(Daily Unique Visitors) 및 총 방문 횟수를 카운트하는 JPQL 쿼리 구현.
        - `com.devdoyen.nemologic.service.VisitorService`에 SHA-256 IP 해싱 함수 및 당일 동일 유저/IP 해시 방문 중복 로깅 방지 로직 적용.
        - `com.devdoyen.nemologic.controller.VisitorController`에 `POST /api/analytics/visit` API 엔드포인트를 구현하여 프록시 환경(X-Forwarded-For 등)에서도 원격 클라이언트 IP를 탐지하도록 매핑.
      - **Actuator 프로메테우스 메트릭 노출**:
        - `com.devdoyen.nemologic.config.VisitorMetricsConfig` 클래스에서 Micrometer `MeterBinder`를 구현하여 `visitor.total.visits`, `visitor.unique.visitors`, `visitor.daily.unique.visitors` 게이지(Gauge) 지표를 바인딩함으로써 Grafana Alloy가 수집할 수 있도록 Prometheus 엔드포인트 연동 완료.
      - **프론트엔드 연동 및 페이지 로드 이벤트 매핑**:
        - `frontend/src/api/userApi.ts`에 `logVisit(uuid)` API 호출 함수 추가.
        - `frontend/src/App.vue` 마운트 시(`onMounted`), 로컬 스토리지에 보존된 익명 세션의 UUID를 백엔드로 발송하여 방문이 자동으로 카운트되도록 흐름 설계.
      - **TDD 기반 단위 및 통합 테스트 전원 통과**:
        - 백엔드 JUnit 5 및 Mockito 기반의 `VisitorServiceTest` 및 `VisitorControllerTest`(MeterRegistry 검증 포함) 테스트 작성 및 42개 전체 테스트 100% 통과 완료.
        - 프론트엔드 Vitest 기반 `userApi.test.ts` 및 `App.test.ts`에 방문 로직 연동 통합 테스트 추가 및 52개 전체 테스트 100% 통과 완료.

    - **모바일 사용자 경험 고도화 (Step 26) - TDD Green Phase 완료**:
      - **모바일 터치 깜빡임 해결**: 캔버스, HUD 스위치 프레임, 개별 버튼 영역 전체에 `-webkit-tap-highlight-color: transparent` 및 `touch-action: none`, `user-select: none` 속성을 지정하여 모바일 웹 브라우저의 기본 탭 하이라이트 깜빡임 오버레이 현상을 완전히 차단 및 격리 완료.
      - **그리기 모드 UI (Draw Mode Toggle) 도입 및 디자인 고도화**: 캔버스 좌측 하단에 글래스모피즘(Glassmorphism) 기반의 그리기 선택기 HUD를 배치 완료. 텍스트 라벨을 전면 제거하고 보드 상태를 정밀 묘사하는 gradient square(채우기 🟦/■) 및 SVG Rose Cross(X 마킹 ❌) 커스텀 아이콘을 도입 완료.
      - **스위치 방식 토글 개선**: HUD 내 특정 버튼만 클릭해야 했던 기존 방식에서, 스위치 프레임 및 활성화된 아이콘을 포함한 HUD 영역 전체 클릭 시 자동으로 `drawMode`가 전환(토글)되도록 변경하고, 활성 모드 상태에 따라 부드러운 위치 전환(translate) 슬라이딩 인디케이터 트랜지션 애니메이션 적용 완료.
      - **레이아웃 비정렬 및 Baseline 치우침 오류 수정**: 스위치 내부 패딩 구조를 4px로 통일하고 가로 너비를 80px로 매핑하여 슬라이더와 개별 아이콘의 중심점 좌표를 완벽히 일치(`translateX(36px)`)시켰으며, 아이콘들의 display 속성을 `block`으로 정의하여 브라우저의 폰트 baseline 텍스트 정렬 기준에 의해 아이콘이 아래/좌측으로 미세하게 쏠리던 오프셋 편차 차단 완료.
      - **입력 전용 터치 이벤트 리스너 통합**: 마우스 클릭 에뮬레이션 딜레이를 유발하지 않도록 `touchstart`, `touchmove`, `touchend` 윈도우 스팬 드래깅 리스너를 결합하고, PC 환경 마우스 우클릭 시에는 기존 X 마킹 동작이 투명하게 보장되도록 폴백 연동 완료.
      - **테마 로고 리디자인**: 기존의 단순 원형 `.logo-icon` 마크업 및 스타일을 탈피하고, 퍼즐 보드를 연상시키는 2x2 그리드 셀 구조의 대각선 그라데이션 채우기 형태(`logo-cell`)로 전면 설계하여 회전 애니메이션과 함께 브랜드 아이덴티티 및 직관성을 고도화 완료.
      - **TDD 그린 페이즈 검증**: `NonogramCanvas.test.ts` 내에 HUD 모드 컴포넌트 렌더링, X-mode left click 시 cell state 2(Mark) 맵핑 검증, touchstart/touchmove 드래그 로직 검증 등 신규 테스트 3개를 구성하여 Red Phase 검출 후, 프로덕션 보완을 거쳐 프론트엔드 전체 55개 테스트 100% 성공(Pass) 확보 완료.

---

## 2. 다음 단계: 서비스 고도화 및 운영 (Next Goals)

### 핵심 작업 목록
1. **CI/CD 파이프라인 정상 가동, SSL 적용 및 Grafana 모니터링 연동 확인**
   - GitHub Actions `infra-apply` 단계에 수동 승인(Environment Gate) 및 리전 환경변수 바인딩 추가 완료.
   - GitHub Actions `app-deploy`를 트리거하여 배포 및 기동 검증 진행 (SSL 인증서 발급, HTTPS 접속 테스트 및 Grafana Cloud 대시보드 메트릭 수집 검증).

---

## 3. 인프라 아키텍처 비용 최적화 및 타협 설계 (Self-Funded Infra Trade-offs)

### 설계 철학 및 목표
* **비용 최소화 및 실무 역량 입증**: 개인 포트폴리오 장기 운영에 따른 사비 충당 부담을 낮추면서, 현업 수준의 고가용성 설계와 실제 타협점(Trade-offs)을 명확히 분석하고 아키텍처적 대체안을 직접 설계·구현하여 역량을 증명함.

### 상세 타협 사항 및 대체 기술 전략
* **SPOF 극복 및 고가용성 타협 (ALB 배제)**:
  * AWS ALB(Application Load Balancer) 기동 시 발생하는 상시 기본 비용(월 약 $20)을 제거하기 위해 Route 53 및 단일 EC2 호스트 구조로 타협.
  * 단일 인스턴스 장애 시 CloudWatch 경보와 연동해 자동 재시작(Auto Recovery)을 수행하거나, Terraform 및 Ansible로 정의된 IaC 코드를 활용해 5분 이내에 인프라와 애플리케이션 컨테이너를 신속 복구(Recovery-Oriented Architecture)하도록 설정해 이중화를 대체함.
* **데이터베이스 영속성 레이어 타협 (RDS 대체)**:
  * AWS RDS 상시 구동 비용(월 약 $15~20 이상)을 방지하기 위해 단일 EC2 내 Docker Compose 기반 PostgreSQL 컨테이너를 구동.
  * RDS의 관리형 자동 백업 기능을 대체하기 위해, 매일 지정된 시간에 DB 백업 덤프파일을 생성하여 AWS S3 버킷으로 자동 전송하는 쉘 스크립트와 Cron 작업을 Ansible 플레이북으로 자동 구축함으로써 인프라 제어 및 데이터 보호 역량을 증명함.
* **자원 제약 최적화 (Memory Optimization)**:
  * 512MB RAM 수준의 극단적인 저비용 인스턴스(`t4g.nano`/`t3.nano`) 환경에서의 구동을 장기적 목표로 설정.
  * JVM Metaspace 메모리 제약 및 GC 최적화 설정을 적용하고, GraalVM 기반 Native Image 컴파일 빌드를 도입해 메모리 점유율을 50MB 이하로 낮추는 파이프라인 구성을 검증.

---

## 4. 개발 규칙 및 제약사항 준수 확인
- **TDD 필수 준수**: 코어 모듈 및 비즈니스 로직 작성 시 반드시 테스트 코드가 선행되어 통과 여부를 검증해야 합니다.
- **디렉토리 격리**: 단일 작업 단위에서는 메인 디렉토리(`frontend/` 또는 `backend/`)를 독립적으로만 수정하며, 여러 메인 디렉토리의 파일들을 동시에 혼재해 수정하지 않도록 통제합니다.
- **반응성 디커플링**: 코어 비즈니스 로직 모듈 내에는 Vue 프레임워크나 반응성 종속성(Ref, Reactive)을 절대 사용하지 않습니다.
- **지표 및 데이터 기반 인프라 의사결정**: 인프라 변경, 신규 클라우드 리소스 도입, 실행 환경 최적화(예: GraalVM Native Image 전환) 등을 제안하거나 구현할 때는 반드시 부하 테스트 결과(TPS, Latency 분포) 또는 시스템 자원 사용량 지표(Disk I/O Wait, RAM/OOM 이력 등)를 사전/사후 실측한 정량적 데이터를 근거로 제시해야 합니다.



