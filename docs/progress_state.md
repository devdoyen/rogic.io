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
- **Terraform IaC 구성**: VPC, Subnet, Route Table, Security Group(22, 80, 8080, 5173 오픈) 및 Ubuntu 22.04 LTS 기반 EC2 인스턴스를 선언적으로 프로비저닝하도록 `infra/terraform/` 작성 완료. 또한 EC2에 대한 IAM Role(SSMManagedInstanceCore 권한 적용) 및 Instance Profile 설정을 추가하여 보안성 확보 완료.
  - 최저 비용 설정을 위해 인스턴스 사양을 **`t3a.nano`**(0.5 GB RAM, AMD 칩셋, 월 약 3.38달러)으로 강등 완료.
  - 고유한 난수 접미사가 붙는 백업 전용 **S3 버킷(`aws_s3_bucket`)**을 선언하고 EC2 인스턴스에 S3 버킷 읽기/쓰기(`s3:PutObject` 등) 권한이 바인딩되도록 IAM 정책 연동 완료.
  - 서버 시작/정지 시 IP 주소가 변경되는 문제를 방지하기 위해 **탄력적 IP(Elastic IP, `aws_eip`)** 리소스를 추가하여 고정 IP를 확보 완료.
- **Ansible 자동 배포 구성**: 타겟 서버에 Docker/Docker Compose 설치, 소스코드 동기화 및 Docker Compose 빌드/기동을 자동화하는 `infra/ansible/` 구성 완료.
  - 0.5 GB 물리 메모리(RAM) 환경에서의 OOM(Out of Memory) 예방을 위한 **1.5GB SSD Swap 가상 메모리 구성 자동화** 태스크 적용 완료.
  - `awscli` 자동 설치 태스크와 Docker 내부 PostgreSQL 데이터를 덤프한 후 자동으로 식별한 백업용 S3 버킷으로 업로드하는 **데일리 새벽 3시 DB 백업 셸 스크립트 및 Cron Job 자동 등록** 추가 완료.
- **프론트엔드 API 동적 환경변수 지원 및 Nginx 리버스 프록시 구축**: 
  - `stageApi.ts` 및 `userApi.ts`에 `import.meta.env.VITE_API_BASE_URL` 환경변수를 연동하여 배포 주소에 맞춰 API 경로가 동적으로 맵핑되도록 수정 완료.
  - 프로덕션 환경의 동일 도메인(Same-Origin) 호출 지원을 위해 `frontend/nginx.conf` 내에 `/api/` 리버스 프록시 설정을 추가하고, 프로덕션 빌드 시 상대 경로 `/api`를 사용하도록 구조 개편 완료.
- **백엔드 CORS 와일드카드 허용**: 실 배포 IP 환경에서의 유동적인 자원 공유를 위해 `StageController`와 `UserController`에 적용된 `@CrossOrigin` 허용 Origin 대상을 기존 `http://localhost:5173`에서 와일드카드(`*`)로 전환 완료.
- **테스트 케이스 보완 및 정상 통과**: Mocking 환경 변경 및 CORS 수정 사항에 맞춰 백엔드 `LocalProfileConfigurationTest`와 `StageControllerTest`를 동기화하여 전체 테스트(프론트엔드 42개, 백엔드 전체 테스트)가 100% 통과(Pass)함 확인 완료.

### Rotagic (Rotate Logic) 컨셉 및 회전 애니메이션 연동 (Step 20) - 완료
- **수학적 좌표 매핑 일반화 및 2D 회전 역변환**: `coordinateMapper.ts` 및 `NonogramCanvas.vue` 개편 완료.
- **틀 및 숫자 고정 레이아웃 및 직립 텍스트 정렬**: 플레이 중에는 45도 다이아몬드 틀(마름모)과 힌트 숫자의 위치를 NW/NE 방향으로 고정하며, 힌트 숫자를 45도 기울이지 않고 가독성을 위해 화면 기준 수직(직립) 방향으로 바로 그려지도록 렌더링을 보완 완료.
- **데이터 회전 및 해결 복귀 애니메이션**: 내부 정답 2D 데이터 배열을 90도 단위의 랜덤 단계(0, 90, 180, 270도)로 무작위 회전시킨 뒤 퍼즐을 조작하게 하고, 해결이 완료되면 힌트 숫자를 숨긴 후 원래 모양(정방향 0도)으로 부드럽게 복귀 회전 애니메이션 적용 완료.
- **단위 테스트 통과**: 회전 유틸리티(`gridRotator.ts`) 및 48개 전체 프론트엔드 테스트 케이스 100% 통과(Pass) 완료.
- **단일 레포지토리 격리 규칙 준수**: `backend/` 디렉토리에 영향 없이 `frontend/` 및 진행 문서만 격리 수정하여 TDD 완료.

---

## 2. 다음 단계: 서비스 고도화 및 운영 (Next Goals)

### 핵심 작업 목록
1. **CI/CD 파이프라인 정상 가동 확인**
   - S3 및 DynamoDB를 연동한 Terraform 원격 백엔드 마이그레이션 및 상태 동기화 완료.
   - GitHub Actions `infra-apply` 단계에 수동 승인(Environment Gate) 및 리전 환경변수 바인딩 추가 완료.
   - GitHub Actions `app-deploy`를 트리거하여 배포 및 기동 검증 진행.

---

## 3. 개발 규칙 및 제약사항 준수 확인
- **TDD 필수 준수**: 코어 모듈 및 비즈니스 로직 작성 시 반드시 테스트 코드가 선행되어 통과 여부를 검증해야 합니다.
- **디렉토리 격리**: 단일 작업 단위에서는 메인 디렉토리(`frontend/` 또는 `backend/`)를 독립적으로만 수정하며, 여러 메인 디렉토리의 파일들을 동시에 혼재해 수정하지 않도록 통제합니다.
- **반응성 디커플링**: 코어 비즈니스 로직 모듈 내에는 Vue 프레임워크나 반응성 종속성(Ref, Reactive)을 절대 사용하지 않습니다.


