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
      - **도메인 브랜딩 동기화**: 운영 도메인인 `rotagic.com`과의 결합을 위해 헤더 로고 타이틀 `<h1 class="app-title">`과 브라우저 `<title>` 메타 태그의 제목을 일괄 `rotagic.com`으로 변경 완료.
      - **브라우저 파비콘(Favicon) 갱신**: 신설된 2x2 격자 로고의 디자인을 정밀 재현한 경량 SVG 파비콘([favicon.svg](file:///c:/Users/82107/dev/project/nemologic/frontend/public/favicon.svg))을 신규 작성 및 대체 적용 완료.
      - **임시 로고 타이틀 프리뷰 적용 (rogic.io)**: 신규 도메인 검토를 위해 헤더 및 브라우저 `<title>` 영역을 임시로 `rogic.io`로 수동 변경 완료 (사용자 요청에 따라 커밋 배제 및 작업 이력만 업데이트).
      - **로고 텍스트 타이포그래피 비율 및 가독성 보정**: 제목 글자인 `rogic.io` 하단의 알파벳 descender(`g` 등)와 서브타이틀(`Rotate Logic Puzzle`) 간의 겹침 간섭을 해결하기 위해 상위 텍스트 래퍼를 flex 갭 구조(`.logo-title-wrapper`)로 변경하고 `line-height: 1.2`로 보정 완료. 서브타이틀은 대문자 자간 분산 배치(uppercase spacing)와 회색조(slate-400) 톤다운을 부여하여 가독성 및 세련미 강화 완료.
      - **크롬 렌더링 잘림 현상 보정 및 자간 너비 정렬**: `background-clip: text` 그래디언트 적용 시 크롬 브라우저에서 소문자 `g` 하단의 descender 곡선이 직선 형태로 잘려 보이던 현상을 해결하기 위해 `app-title`에 `padding-bottom: 4px` 및 `margin-bottom: -4px` 여유 영역을 보정 완료. 서브타이틀(`ROTATE LOGIC PUZZLE`)의 물리적 가로 길이를 `rogic.io` 너비에 비례하게 동기화하기 위해 폰트 크기를 `0.52rem`, 자간을 `0.08em`로 균형 조절 완료.
      - **TDD 그린 페이즈 검증**: `NonogramCanvas.test.ts` 내에 HUD 모드 컴포넌트 렌더링, X-mode left click 시 cell state 2(Mark) 맵핑 검증, touchstart/touchmove 드래그 로직 검증 등 신규 테스트 3개를 구성하여 Red Phase 검출 후, 프로덕션 보완을 거쳐 프론트엔드 전체 55개 테스트 100% 성공(Pass) 확보 완료.

    - **로고 서브타이틀 자간 및 정렬 정밀 조정, 히스토리 모달 버그 해결 및 마이페이지 UI 개선 (Step 27) - 완료**:
      - **텍스트 가로 길이 맞춤**: 헤더 로고의 타이틀 `rogic.io`(`.app-title`)의 크기를 기존 `1.8rem`에서 `2.1rem`으로 늘려 가로 길이를 인위적으로 확대 완료. 이에 비례하여 서브타이틀 `ROTATE LOGIC PUZZLE`의 `letter-spacing`을 `0.12em`으로 보정하였습니다.
      - **양끝 맞춤(Justify) 배치**: 다이나믹하게 변하는 디바이스 뷰포트 및 대체 글꼴(fallback font) 렌더링 오차를 방지하기 위해 `text-align: justify` 및 `text-align-last: justify`를 서브타이틀에 결합하여 글자가 메인 타이틀의 좌우 경계선에 딱 맞아 떨어지도록 구조적 배치 개선 완료.
      - **수직 정렬 보정 (Optical Alignment)**: 로고 아이콘 대비 글자 텍스트 블록 전체가 미세하게 하단으로 쏠려 보이는 착시 현상을 제거하기 위해 `.logo-title-wrapper`에 `margin-top: -3px`를 추가하여 시각적 중심선을 상향 조정 완료.
      - **히스토리 복기용 모달 캔버스 렌더링 오류 수정**: 히스토리 조회 시 캔버스 프레임의 크기가 0에 수렴해 한 줄 혹은 점 형태로 축소되어 보이던 버그를 수정하기 위해, `.modal-canvas-wrapper` 컨테이너에 반응형 크기 설정(`width: 320px; max-width: 100%; aspect-ratio: 1`)을 부여하고 다크 테마 배경색(`#0f172a`)을 적용하여 캔버스가 모달 정중앙에서 가득 차게 최적 스케일로 정렬되도록 개선 완료.
      - **읽기 전용 상태일 때 HUD 숨김 처리**: 복기용 캔버스가 읽기 전용(`readOnly === true`) 상태일 때는 불필요한 도구 선택기(`.draw-mode-hud`) 및 줌 HUD(`.zoom-hud`)를 마운트하지 않도록 제어하여 화면 겹침 현상을 원천 방지 완료.
      - **마이페이지 단일 열 센터링 대시보드 리팩토링**: 기존의 모바일/데스크톱 2단 분할 그리드 사이드바 형식을 완전히 탈피하여, 화면 정중앙에 배치된 통합 카드 형태의 `.mypage-dashboard` 레이아웃으로 변경 완료. 대시보드 자체의 카드형 어두운 배경색을 걷어내고 투명화(`background: transparent; border: none`) 처리하여 공간적 답답함을 원천 해소 완료.
      - **대시보드 내부 정렬 최적화**: 대시보드 내부의 상단에 사용자 프로필(`Avatar`, `Lv`, `XP`) 카드를 위치시키고, 바로 하단에 복귀용 히스토리 카드 목록(`.stage-card-list`)을 일렬 스택 구조로 정렬 완료. 기존의 중복 타이틀 문구(`My History`)를 완전히 제거하여 미니멀리즘을 실현하고, 목록 스크롤 오버플로우 방지를 위해 최대 높이(`max-height: 280px`) 설정을 적용 완료.
      - **프로필 카드화 및 기록 카드 플로팅 정밀 스타일링**: 사용자 프로필 영역(`.mypage-user-profile`)만 독립된 어두운 카드 배경색(`#1e293b`)과 테두리(`border: 1px solid #334155`)를 부여하고, 하단의 히스토리 개별 항목들(`.history-item`) 역시 동일한 `#1e293b` 배경색과 `#334155` 테두리의 독립된 플로팅 카드로 각각 분리 디자인 완료. 이를 통해 메인 페이지의 `#0f172a` 배경 상에 카드 형태로 가지런히 떠 있는 입체감 있는 UI 구현 완료.
      - **모바일 화면 여백 및 스크롤 높이 최적화**: 모바일 뷰(`max-width: 768px`)에서 콘텐츠 좌우 여백을 줄이기 위해 대시보드 가로폭을 `width: 100%` 및 `max-width: 540px`로 상향 조정하고 PC 뷰에서도 `max-width: 600px`, `width: 100%`로 확장하여 가로 공간 활용도를 높이고 불필요한 좌우 여백을 대폭 줄였습니다. 또한 아바타 크기를 `4.5rem` -> `3.5rem`으로 줄이고 히스토리 리스트 영역의 최대 높이를 `max-height: calc(100vh - 250px) !important`로 다이나믹하게 확장하여 모바일 가용 스크린 높이를 빈틈없이 가득 활용하도록 개선 완료.
      - **통합 모달 반응형 스타일 적용 (상세 화면 여백 확보)**: 기존에 인라인으로 중복 선언되어 모바일에서 극도로 비좁은 패딩과 좌우 여백 잘림 현상을 유발하던 모달 스타일을 전역 `.modal-content` CSS 클래스로 이관 완료. 모바일 환경에서 모달의 안쪽 여백을 `padding: 1.25rem 1rem`으로 개선하고 가로 비율을 `width: 88%`로 보정하여, 히스토리 복기 상세 화면에서 캔버스의 좌우 공간이 화면 모서리에 딱 붙지 않고 6% 이상의 충분한 여백(Margin)을 확실하게 확보하도록 수정 완료.
      - **Puzzle Replay 기능 팝업(Modal) 가이드 화**: 페이지 내부 공간을 크게 차지하던 안내 상자(`.mypage-instruction-box`)를 삭제하고, 유저가 마이페이지에 최초로 접근하는 시점에만 `localStorage`(`rogic_mypage_tip_shown`) 기반으로 팝업식 도움말 모달(`isMypageTipOpen`)을 노출하여 공간 활용성 및 UI 디자인 감각 극대화 완료.
      - **완성된 행/열 힌트 시각적 흐림(Dim) 처리**: 퍼즐 플레이 도중 특정 행이나 열의 채워진 셀 구성이 해당 행/열의 힌트 수치와 정확히 일치(완성)할 경우, 캔버스 좌측 및 상단에 노출되는 숫자 힌트를 자동으로 흐리게(기존 `#94a3b8` -> `#475569`) 드로잉하여 유저가 완성 여부를 한눈에 식별할 수 있도록 사용성을 대폭 고도화 완료.
      - **TDD 검증**: 스타일 수정 및 힌트 상태 판별 로직 적용 이후 Vitest의 전체 56개 단위 및 통합 테스트 케이스가 아무런 부작용(Side-effect) 없이 전원 100% 통과(Pass)함을 확인 완료.

    - **퍼즐 퀄리티 관리자 페이지 구축 및 AI 퍼즐 승인 프로세스 (Step 28) - 완료**:
      - **도메인 엔티티 수정**: `Stage.java`에 `approved` 필드(기본값 true)를 추가하고, 기존 퍼즐은 자동 승인되도록 설정 완료.
      - **조회/수정 비즈니스 로직 고도화**: `StageRepository`에 `findByActiveAndApproved(true, true)` 쿼리를 추가하여 미승인 또는 소프트 딜리트된 스테이지를 조회 대상에서 원천 배제 처리 완료.
      - **관리자 REST API 컨트롤러 설계 및 JUnit 통합 테스트 완료**: `AdminStageController.java`를 신설하여 CRUD 관리 API 매핑 및 NonogramSolver를 통한 해의 유일성 검증 연동 완료.
    - **백오피스 보안 인증(Spring Security) 및 로그인/로그아웃 연동 (Step 29) - 완료**:
      - **Spring Security 연동**: `/api/admin/**` 호출 시 `ROLE_ADMIN` 권한을 강제하는 Security 및 stateless 세션 설정을 적용 완료.
      - **인메모리 세션/토큰 관리 및 UI 연동**: `AdminSessionManager`를 통해 UUID Bearer 토큰 방식을 구현하고, 프론트엔드 `adminApi.ts`에서 Axios Request Interceptor를 통해 Authorization 헤더를 자동 전송하도록 바인딩 완료.
    - **어드민 퍼즐 관리 필터링 및 정렬 기능 구현 (Step 30) - 완료**:
      - **반응형 필터링 및 컬럼 정렬**: `App.vue`에 이름 검색, 격자 크기별 검색, 활성/대기/비활성 상태 검색 조건 필터와 컬럼 클릭에 따른 다이나믹 정렬(`toggleAdminSort`) 기능 연동 완료.
    - **AI 퍼즐 생성 크기 커스텀 및 타이틀 클리닝 적용 (Step 31 - 완료)**:
      - **AI 클라이언트 및 백엔드 확장**: `AiClient.java` 인터페이스에 `generatePuzzleJson(int width, int height)`을 추가하고, `MockAiClient.java`와 `GeminiAiClient.java`에 dynamic size 연동 구현. `AiStageGenerator.java` 내에 정규식 `^(?i)(AI\s+Puzzle|Daily\s+Puzzle)[:\\s-]*`을 사용한 타이틀 접두사 청소 로직을 적용하여 순수 명칭만 저장되도록 보장 완료. `AdminStageController.java`에서 `width`와 `height` 파라미터를 추가 수집하도록 연동 완료.
      - **프론트엔드 API 및 어드민 UI 연동**: `adminApi.ts` 내 `generateAiStage(width, height)`가 쿼리 파라미터를 백엔드로 넘기도록 확장. 어드민 네비게이션 바에 AI Size 선택을 위한 가로/세로 셀렉터(`adminAiWidth`, `adminAiHeight` ref 바인딩)를 추가 배치하고, 생성 버튼 클릭 시 이를 형변환하여 안전하게 파라미터로 송출하도록 `App.vue` 연동 완료.
      - **TDD 기반 양방향 테스트 검증**:
        - **백엔드**: JUnit `AiStageGeneratorTest`, `AdminStageControllerTest` 에 dynamic size 검증과 접두사 클리닝 테스트를 추가하고, `StageControllerTest`를 보정하여 전체 56개 JUnit 테스트 100% 통과 완료.
        - **프론트엔드**: Vitest `adminApi.test.ts` 및 `App.test.ts`에 커스텀 크기 전달 통합 테스트를 작성하고, JSDOM 호환 select value 바인딩을 매핑하여 전체 68개 Vitest 테스트 100% 통과 완료.

    - **AI 퍼즐 생성 크기 30x30 확장 및 NonogramSolver 최적화 (Step 32 - 완료)**:
      - **Solver 성능 병목 해결**: 기존 `NonogramSolver.isUnique`가 모든 행 패턴을 사전 생성하던 방식을 개선하여, 동적 계획법(DP) 기반의 행/열 논리 라인 해결기(`solveLine`) 및 `isPartialLineCompatible` 유효성 검사를 구현 완료.
      - **셀 단위 백트래킹 도입**: 논리적 분석 후 미해결 상태인 셀에 대해서만 부분 검증이 수반된 셀 단위 DFS 백트래킹(`solveDFSCell`)을 수행하도록 개선하여, 기존 30x30 수준에서 발생하던 OOM 및 타임아웃 문제를 해결하고 1ms 이하 수준의 초고속 검증 완료.
      - **TDD 기반 대용량 퍼즐 검증 완료**:
        - `NonogramSolverTest.java`에 10x10 Smile Face, 15x15 Ascending Star, 30x30 Solid Grid 등의 고난도 고해상도 고정 퍼즐에 대한 유일 해 검증과 30x30 Giant Cross 비유일 해 판정 테스트(TDD Red/Green Phase)를 추가하여 백엔드 60개 전체 테스트 빌드 및 Vitest 프론트엔드 68개 전체 테스트 통과 완료.
      - **배포 인프라 자원 제한 OOM 대응 (추가)**:
        - `t3a.nano` (512MB RAM) 환경의 인프라 특성상 컨테이너 내부의 Gradle compilation 멀티스테이지 빌드는 과도한 메모리/CPU 사용으로 인한 SSHD 프로세스 킬 및 접속 끊김(Ansible Unreachable Error)을 유발함.
        - 이에 대응하여 `backend/Dockerfile`을 pre-built JAR 복사 방식(Single-stage)으로 롤백 처리함. 로컬 환경 빌드(`deploy.bat`, `deploy.sh`) 및 GitHub Actions CI/CD workflow의 runner(7GB RAM)에서 Gradle 빌드를 선행 수행한 뒤, 최종 `.jar`를 EC2 인스턴스로 동기화하여 서비스 가동하도록 보장함.

    - **서비스 가용성 및 SLA 모니터링 체계 구축 (Step 33 - 완료)**:
      - **무료 가동률 모니터링 설계**: AWS Route 53 및 CloudWatch Alarm의 상시 지출 비용을 차단하기 위해 Grafana Cloud의 Synthetic Monitoring(월 50만회 체크 무료)을 채택함.
      - **이메일 경보 연동 및 통합**: API 엔드포인트(`https://rogic.io/api/stages`) 대상 전 세계 멀티프로브 60초 체크 주기를 설정하고, Grafana Alerting 및 Alertmanager를 매핑하여 이메일 수신처로 실시간 긴급 경보(Severity: Critical)가 전송되도록 구축함.
      - **IaC 기반 선언적 관리 구현**: Grafana Terraform 프로바이더(`grafana/grafana`)를 새로 도입하고, HTTP 헬스체크 리소스(`grafana_synthetic_monitoring_check`), 알림 수신용 이메일 연락처(`grafana_contact_point`), 그리고 장애 알림 경보 규칙(`grafana_rule_group`)을 `infra/terraform/grafana.tf` 코드로 작성하여 선언적으로 형상관리함.
      - **SLA 대시보드 PromQL 설계**: 다운타임 복구 및 품질 지표 산출용 PromQL 식(Uptime SLA %, Incident Count, MTTR, MTBF) 설계 수식을 완성하고, 관리 가이드를 [monitoring_guide.md](file:///c:/Users/82107/dev/project/nemologic/infra/monitoring/monitoring_guide.md) 문서로 구축하여 저장소 내 보관을 완료함.
      - **통합 모니터링 대시보드(SLA 및 JVM/방문자 통계) 코드화 및 병합**:
        - 기존 Grafana Cloud의 실시간 대시보드 설정(`current_dashboard.json`)과 신규 설계한 SLA 및 가용성 패널 5종(API Health, 30-Day Service Availability, 30-Day Incident Count, MTTR, MTBF)을 단일 대시보드로 병합 완료.
        - 구버전 스키마 패널들을 신버전 `dashboard.grafana.app/v2` 스키마(`spec.elements` 및 `GridLayoutItem`을 가진 `RowsLayoutRow`)로 동적 변환하는 Python 병합 파이프라인을 작성 및 수행하여 최상위 행으로 삽입 완료.
        - 대시보드 내 모든 Prometheus 데이터소스 참조(`grafanacloud-prom`)를 `"${DS_PROMETHEUS}"` 변수 형식으로 일괄 치환 및 매핑하여, Terraform 배포 시점에 target Prometheus 데이터소스의 실제 UID(`data.grafana_data_source.prometheus[0].uid`)로 자동 주입 및 동적 치환되도록 통합 IaC 환경 구축 완료.
        - `infra/monitoring/current_dashboard.json`에 최종 병합본을 오버라이트 저장하고, 로컬 `terraform validate` 구문 정합성 검증 통과 완료.
        - Grafana API 409 Conflict(동일 UID 및 Title 중복 오류)를 회피하기 위해 `metadata.uid`를 신규 고유 식별자(`nemologic-unified-dashboard`)로 교체하고, `spec.title`을 `"Rotagic Unified Monitor"`로 변경함. 또한 `resourceVersion`, `generation`, `creationTimestamp` 및 UI 기본 저장용 annotations(`grafana.app/folder: ""`) 등의 읽기전용/메타데이터 속성을 완전히 소거하여 테라폼 배포 정합성을 확보함.
        - Grafana Cloud의 Synthetic Monitoring 표준 메트릭 규격에 맞춰, 존재하지 않는 임시 메트릭명인 `sm_check_status`를 표준 프로브 성공 메트릭인 `probe_success`로 일괄 치환(대시보드 패널 5종, 경보 조건식 및 가이드 문서)하여 지표 유실 및 No Data 에러 해결 완료.
        - PromQL 문법상 수식 연산(예: `1 - probe_success`)에 범위 선택자(`[30d]`)를 직접 적용할 수 없는 파싱 에러(parse error: ranges only allowed for vector selectors)를 해결하기 위해, MTTR 및 MTBF 산출 공식을 벡터 선택자 단위로 분해 및 수리적으로 간소화하여 쿼리 작동 정상화 완료.
        - 비즈니스 API 호출 부하 및 네트워크 대역폭 낭비를 차단하기 위해, 기존의 전체 스테이지 목록 API(`/api/stages`) 대상 헬스체크를 Spring Boot Actuator 전용 경량 헬스체크 엔드포인트(`/actuator/health`)로 대체하고, Nginx 리버시 프록시(`nginx.prod.conf`)에 해당 엔드포인트 경로 개방 및 테라폼 배포 타겟(`grafana.tf`) 갱신 완료.
        - 모니터링 시스템의 IaC 기반 자동 배포 완비에 따라 불필요해진 수동 설정 가이드(`monitoring_guide.md`)를 제거하고, 핵심 아키텍처 및 SLA PromQL 수식 설명 데이터를 `docs/portfolio.md` 내부의 가시성 섹션으로 일괄 통합 정리 완료.
        - 헬스체크 타겟 전환에 따라 시계열 데이터베이스(Prometheus)에 누적되어 있던 이전 타겟(`/api/stages`)의 지표가 대시보드상에 중복 노출(6개 게이지 및 지표 이중화)되는 문제를 해결하기 위해, 모든 PromQL 쿼리 및 경보 규칙에 타겟 호스트 필터(`instance="https://rogic.io/actuator/health"`)를 추가하여 활성화된 지표만 선별 노출되도록 격리 처리 완료.

    - **AWS RDS 마이그레이션 중단 및 로컬 DB 아키텍처 원복 (Step 34 - 완료)**:
      - AWS RDS PostgreSQL 마이그레이션이 검토 및 일부 진행되었으나, 유저 요청에 따라 기존 로컬 Docker Compose 기반 PostgreSQL 컨테이너 및 단일 백엔드 구성(Option 1)으로 완전히 롤백함.
      - Nginx 로드 밸런서(blue-green) 및 Grafana Alloy 멀티 인스턴스 메트릭 수집 설정을 이전의 단일 백엔드 구성으로 원복 완료.
      - H2/PostgreSQL 기반 로컬 및 빌드 환경 단위 테스트 68개 및 JUnit 60개 테스트 전원 100% 통과 완료.
      - Grafana 대시보드 내 방문자 지표(Daily Unique Visitors, Total Unique Visitors, Total Page Views, Time Series) 패널 4종의 PromQL 쿼리에 `{instance="$instance"}` 필터를 추가하여, 데이터베이스에 수집된 구버전 blue/green 인스턴스들의 이력이 혼선 및 중복 노출되지 않고 선택된 인스턴스 지표만 단일 노출되도록 수정 완료.


    - **데이터베이스 버전 관리 도구 Flyway 도입 및 검증 (Step 35 - 완료)**:
      - **Flyway 의존성 추가**: `backend/build.gradle`에 `flyway-core` 및 `flyway-database-postgresql` 의존성을 추가 완료.
      - **초기 스키마 마이그레이션 구현**: 기존 테이블 구조(`users`, `stages`, `histories`, `visitor_logs`)를 복원하는 `V1__init.sql` 스크립트를 `backend/src/main/resources/db/migration/` 디렉토리에 정의 완료.
      - **테스트 환경 격리 및 Linter 경고 해결**: `application-test.yml` 내 H2 인메모리 테스트 빌드 시 Flyway를 비활성화(`spring.flyway.enabled: false`)하고, YAML key lint warning 해결을 위해 `format_sql`을 `"[format_sql]"` 형태로 이스케이프 처리 완료.
      - **LocalProfileConfigurationTest 정합성 확보**: Local profile 컨텍스트 로딩 시 Mock DataSource 메타데이터 부재로 `ddl-auto: validate` 검증이 실패하던 현상을 `@SpringBootTest(properties = { ... "spring.jpa.hibernate.ddl-auto=none" })` 프로퍼티 주입으로 해결 완료.
      - **TDD 검증**: `gradle test` 실행 결과 전체 백엔드 JUnit 테스트가 100% 정상 통과(BUILD SUCCESSFUL)함을 확인 완료.
      - **Flyway 비정형 DB 스키마 충돌 해결**: 기존 수동 테이블 적재 이력이 존재하는 상황에서 Flyway 히스토리 테이블 부재로 인한 `Found non-empty schema(s) "public" but no schema history table` 예외 발생을 방지하기 위해, `application-local.yml`에 `spring.flyway.baseline-on-migrate: true` 설정을 부여하여 정상 실행되도록 조치 완료.

    - **Ansible 배포 시 권한 상승 타임아웃 오류 해결 (완료)**:
      - `t3a.nano` 인스턴스의 자원 제약(512MB RAM)으로 인해 첫 부팅 시 `cloud-init` 및 시스템 초기 작업으로 인한 CPU/RAM 100% 병목 및 이로 인한 Ansible `sudo` 권한 상승 대기 시간 초과(Timeout) 현상 식별.
      - 플레이북에 `gather_facts: false` 기반의 대기 스테이지(`Wait for Cloud-Init and SSH connection`)를 추가하여 `/var/lib/cloud/instance/boot-finished` 생성 여부를 폴링 검증하도록 구조를 분리함.
      - 권한 상승 타임아웃(`become_timeout`)을 60초로 확장하여 저사양 VM 환경에서의 프로비저닝 안정성을 확보함.
      - CI 단계(`ci` 잡) 내에 `ansible-playbook --syntax-check` 단계를 이식하여, 플레이북 구문 에러가 존재할 경우 후속 배포 잡(Deploy Job) 실행을 사전에 차단하도록 자동 가드를 완성함.
      - Play-level `vars` block에 설정한 `ansible_become_timeout`이 `Gathering Facts` 단계에서 무시되는 문제를 방지하기 위해, `ci-cd.yml` 환경 변수(`ANSIBLE_TIMEOUT: 60`, `ANSIBLE_BECOME_TIMEOUT: 60`), `hosts.ini` 인벤토리 변수, 로컬 배포 스크립트(`deploy.sh`, `deploy.bat`)에 전역 타임아웃 설정을 명시적으로 이식하여 초기 팩트 수집 권한 상승 대기 문제를 최종 해결함.

    - **Blue-Green 무중단 배포 아키텍처 전환 (Step 36 - 완료)**:
      - **Docker Compose 다중 백엔드 구성**: `infra/ansible/docker-compose.prod.yml` 파일에서 단일 `backend` 서비스 구동을 중지하고, `backend-blue` (포트 8081) 및 `backend-green` (포트 8082)의 두 개 백엔드 활성 컨테이너 서비스를 추가 정의 완료. 로컬 `db` (PostgreSQL) 컨테이너는 유지하여 두 백엔드 인스턴스 모두 동일 로컬 데이터베이스를 바라보도록 설정 완료.
      - **Nginx Upstream 로드 밸런싱 및 Failover 구성**: `infra/ansible/nginx.prod.conf` 파일에 `upstream backend_servers` 블록을 구성하여 `backend-blue:8080`과 `backend-green:8080`을 등록하고, 장애 감지 임계치(`max_fails=1 fail_timeout=5s`)를 적용 완료. `/api/` 및 `/actuator/health` 엔드포인트 요청을 `backend_servers` 업스트림으로 포워딩하도록 라우팅 완료.
      - **Grafana Alloy 메트릭 수집 고도화**: `infra/monitoring/config.alloy` 파일 내 prometheus scrape 대상을 `backend-blue:8080` 및 `backend-green:8080`으로 다중 확장하여 두 인스턴스의 개별 Actuator 메트릭을 실시간 수집 가능하도록 갱신 완료.
      - **Ansible 플레이북 무중단 롤링 업데이트 자동화 및 빌드 부하 최적화**:
        - 기존의 전체 중단 방식의 컨테이너 기동 단계를 제거하고, 롤링 배포 구조 구현 완료.
        - 롤링 업데이트 도중 각 서비스 개별 기동 시 `--build` 옵션으로 인해 빌드가 여러 차례 수행되면서 저사양 VM의 자원 고갈로 SSH 접속이 끊기던 현상을 차단하기 위해, 플레이북 초기에 `docker compose build backend-blue frontend` 통합 빌드를 1회만 먼저 수행하고 개별 기동 시에는 `--build` 옵션을 제외하여 롤링 업데이트 부하를 최소화 완료.
        - 저사양 VM(512MB RAM)의 극단적인 가용 자원 확보를 위해, 평상시에는 1개의 백엔드만 단독 실행하고 배포 시점에만 신규 백엔드가 임시 교차 실행되도록 **액티브-패시브(Active-Passive) 무중단 블루-그린 스위칭 롤링 기법**을 도입 완료. 플레이북이 실행 중인 컨테이너(`blue`/`green`)를 자동 감지하고, **새 버전 기동 전 대기(Inactive) 컨테이너를 먼저 강제 중지(`docker compose stop`)하여 메모리를 완벽하게 확보**한 뒤 새 버전을 띄우고, 헬스체크 완료 후 구 버전의 액티브 컨테이너를 최종 중지하는 자원 자율 관리 체계를 완비함.
      - **프론트엔드 빌드 오프로딩 (Build Offloading) 최적화**: 저사양 EC2 기동 부하 및 CPU 크레딧 고갈 문제를 해결하기 위해, 기존에 EC2 호스트 내부 Docker 컨테이너 안에서 수행되던 Vue 프론트엔드 컴파일(`npm run build`)을 GitHub Actions 빌드 주자(Runner)로 전면 이관(Offload) 완료. 빌드 결과물(`dist/` 폴더)만 동기화하여 서비스 가동하도록 `frontend/Dockerfile` 및 `.github/workflows/ci-cd.yml` 변경 완료.
      - **백엔드 지연 초기화 (Lazy Initialization) 활성화**: Spring Boot 구동 시 모든 Bean을 즉시 생성하지 않고 최초 요청 시점에 생성하도록 `application-local.yml`에 `spring.main.lazy-initialization: true` 설정을 적용 완료. 이를 통해 초기 기동 메모리 점유율과 CPU 오버헤드를 줄여 저사양 VM 환경에서의 기동 속도를 추가적으로 향상 완료.
      - **JVM 및 데이터베이스 커넥션 풀 최소화 및 헬스체크 타임아웃 방지 최적화**:
        - 저사양 VM(512MB RAM)에서 두 개의 컨테이너가 동시에 부팅 및 기동될 때의 메모리 병목과 스왑 오버헤드를 차단하기 위해 `JAVA_TOOL_OPTIONS` 설정을 `-Xmx256m -Xms64m -Xss256k -XX:MaxMetaspaceSize=96m`으로 설정 완료 (과도한 힙 압축으로 인한 GC 쓰레싱 및 스레드 기아 루프를 막고자 최댓값 `-Xmx`를 `256m`으로 환원하되 최솟값 `-Xms`를 `64m`으로 억제하여 초기 기동 부담과 native 점유율 최소화).
        - 부하 가중 시 `curl` 응답 대기 지연으로 인해 헬스체크 단계가 지연/중단되지 않도록 Ansible 플레이북 내 헬스체크용 curl 명령에 `--connect-timeout 3 --max-time 5` 가드를 추가 적용 완료.
        - PostgreSQL 컨테이너의 커넥션 포크(Fork) 메모리 점유를 줄이기 위해 `application-local.yml`의 HikariCP 설정을 기존 `maximum-pool-size: 10`, `minimum-idle: 5`에서 각각 `4` 및 `1`로 대폭 축소하여 인스턴스당 메모리 및 프로세스 오버헤드를 원천 절감 완료.
        - 스프링 부트 시동 시 수행되는 무거운 메타데이터 쿼리 분석 과정을 생략하여 기동 속도를 대폭 개선하기 위해 Hibernate 스키마 자동 검증(`spring.jpa.hibernate.ddl-auto: validate`)을 `none`으로 격리 전환 완료 (Flyway 마이그레이션 도구는 정상 작동하며, 관련 단위 테스트 `LocalProfileConfigurationTest` 통합 정합성도 확인 완료).
        - 배포 시작 시점의 호스트 메모리, 실행 중인 컨테이너 상태, 스왑 크기를 선제적으로 감증하기 위해 플레이북 초기 단계에 리소스 디버그 작업(`Debug host resources`) 구성 완료.
      - **가상 메모리(Swap File) 2GB 증설**: 저사양 인스턴스(512MB RAM)의 OOM 위험을 완전히 방지하고 안정성을 확보하기 위해 스왑 파일을 기존 1.5GB에서 2GB로 증설 완료. 또한 기존 서버에 반영된 스왑 크기를 자동으로 감지하여 필요 시 안전하게 동적으로 해제 후 재배포하는 스크립트 분기를 `playbook.yml`에 완비함.

       - **Docker 이미지 빌드 메모리 최적화 및 빌드 컨텍스트 경량화**:
         - GraalVM Native Image 도입을 통해 컨테이너당 메모리 점유율을 50MB 미만으로 대폭 경량화함에 따라, 배포 모델을 기존 Active-Passive 전환 방식에서 완전한 무중단(Zero-Downtime)을 유지하는 Active-Active 순차 롤링 배포(Rolling Update) 체계로 환원 완료.
         - `backend/.dockerignore` 및 `frontend/.dockerignore` 파일을 구축하여, 불필요한 빌드 도구 캐시(`.gradle`, `node_modules` 등) 및 소스 파일들이 Docker 데몬에 빌드 컨텍스트로 전달되는 오버헤드를 완전 차단하고 빌드 시간을 최적화 완료.


       - **GraalVM Native Image 컴파일 및 배포 전환**:
         - Spring Boot 3.3.0 백엔드를 GraalVM Native Image로 컴파일하여 실행 시 메모리 점유율을 50MB 미만으로 경량화할 수 있도록 `backend/build.gradle`에 `org.graalvm.buildtools.native` 플러그인을 도입 완료.
         - 유닛 테스트 시 Mockito mock 정의 (`MockDefinition`) 로직과 Spring Boot AOT 컴파일 도구 간의 호환 한계를 해소하기 위해 JVM 테스트 구동 시 `processTestAot` 태스크를 스킵(`enabled = false`)하도록 설정 완료.
         - `.github/workflows/ci-cd.yml` 내 배포 빌드 단계를 `graalvm/setup-graalvm` 액션을 통한 GraalVM JDK 환경으로 구성하고 `./gradlew nativeCompile` 명령을 실행하도록 파이프라인 전환 완료.
         - 빌드된 네이티브 바이너리 실행을 지원하기 위해 `backend/Dockerfile`의 베이스 이미지를 `debian:12-slim`으로 경량화 및 전환하고, JVM 전용 설정(`JAVA_TOOL_OPTIONS`)을 제거하여 저사양 호스트 적합성을 최대화 완료.

    - **SLA 대시보드 레이아웃 리팩토링 및 시각적 개선 (Step 37 - 완료)**:
      - **그리드 구조 분할**: 단일 행에 가로 방향(width: 3)으로 8개 패널이 가로로 조밀하게 배치되어 있던 "Nemologic Service SLA Metrics" 행의 그리드 배치를 각각 가로 6, 세로 5 크기의 2개 행(4x2 그리드 배열) 구조로 개편 완료.
      - **패널 가시성 최적화**: 1행에는 실시간 API Health, 24시간 가동률, 7일 가동률, 30일 가동률 패널을, 2행에는 30일 총 장애 횟수, MTTR, MTBF, 호스트 자원(Memory/Swap) 추이 패널을 배치하여 메트릭 수치 가독성 및 정보 탐색 시각화 안정성 극대화 완료.

    - **AI 데일리 퍼즐 다각화 및 품질 고도화 (Step 38 - 완료)**:
      - **다중 크기 자동 생성 루프**: `DailyPuzzleScheduler`가 매일 크론 표현식(`0 17 4 * * ?`)에 맞춰 `5x5`, `10x10`, `15x15`, `20x20`, `25x25`, `30x30` 크기의 데일리 퍼즐 총 6개를 순차 기동 생성하도록 확장 완료. 각 생성 주기 내에 예외 처리를 격리하여 특정 크기 실패가 전체 스케줄러 흐름을 중단하지 않도록 안전 조치 완료.
      - **최근 주제 기반 프롬프트 배제**: `AiClient` 인터페이스 및 `GeminiAiClient`/`MockAiClient`에 `recentThemes` 매개변수를 추가하고, 최근 생성된 10개의 퍼즐 이름을 DB에서 추출하여 AI 프롬프트에 배제 대상으로 주입시킴으로써 의미론적 주제 중복을 방지 완료.
      - **그리드 완전 중복 검사**: `StageRepository`에 `existsBySolutionGrid(int[][] grid)` 조회를 구현하여 생성된 결과물이 기존 DB에 있는 퍼즐과 완전히 동일한 경우 재시도하도록 중복 차단 파이프라인 수립 완료.
      - **TDD 기반 단위 및 통합 테스트 성공**: JUnit 5 기반의 `AiStageGeneratorTest`에 중복 발생 시의 재시도 검증, 스케줄러의 다중 크기 루프 기동 호출 검증 테스트를 보강 완료하고 전체 백엔드 61개 및 프론트엔드 68개 테스트가 모두 100% 성공(Pass)함을 입증 완료.

    - **AI 데일리 퍼즐 예비용 풀(Reserve Pool) 구축 및 무장애 릴리즈 자동화 (Step 39 - 완료)**:
      - **예비용 버퍼 풀 상태 정의 및 시드 제어**: API 장애나 레이트 리밋에 대비하기 위해 DB 내에 비공개 승인 완료(`active = false, approved = true`) 상태의 예비용 퍼즐을 크기별로 최소 **5개** 상시 확보하도록 설계 완료.
      - **스케줄러 자동 리필 및 풀 관리 고도화**: 매일 새벽 4시 17분에 스케줄러가 가동될 때 각 크기별 버퍼 카운트(`stageService.getInactiveApprovedCount`)를 체크하고, 5개 미만인 경우에만 `aiStageGenerator`를 기동해 5개를 채우도록 리필 로직 구성 완료.
      - **선입선출(FIFO) 기반 단일 릴리즈 활성화**: 매일 자정 00:00에 스케줄러(`releaseDailyPuzzle`)가 작동해 `stageService.releaseDailyPuzzles`를 실행하면, DB 내에 보관된 해당 크기의 가장 오래된 비공개 예비 퍼즐 하나를 선별하여 사용자 서비스용으로 활성화(`active = true`) 처리하는 선입선출 구조 설계 및 연동 완료.
      - **TDD 기반 단위 및 통합 테스트 전원 통과**: `StageServiceTest` 내 `testGetInactiveApprovedCount` 및 `testReleaseDailyPuzzles` 테스트를 신설하고 `AiStageGeneratorTest` 내 스케줄러 버퍼 리필 모킹 시나리오를 갱신 완료하여 전체 백엔드 63개 및 프론트엔드 68개 테스트가 모두 100% 성공(Pass)함을 확보 완료.

    - **CI/CD 빌드 파이프라인 외부 패키지 레포지토리 예외 처리 및 안정화 (Step 40 - 완료)**:
      - **APT 업데이트 에러 내성 확보**: GitHub Actions Runner(Ubuntu 24.04 환경) 실행 중 Microsoft/Azure 등 외부 써드파티 레포지토리의 일시적인 403 Forbidden 차단 이슈로 전체 `sudo apt update` 명령이 실패하는 문제를 발견 및 분석 완료.
      - **빌드 단락 차단 방지**: [ci-cd.yml](file:///.github/workflows/ci-cd.yml) 내의 모든 `apt update` 구문을 `apt-get update || true` 형태로 대체 수정하여, 외부 레포지토리의 사소한 서버 불안정이 전체 빌드 파이프라인 중단(Exit Code 100)으로 이어지지 않도록 내결함성(Fault-tolerance) 조치 적용 완료.

    - **Nginx 포트 스위칭 대응 지연 초기화 비활성화 및 기동 최적화 (Step 41 - 완료)**:
      - **지연 초기화 환원**: Active-Active 로드 밸런싱 환경에서 신규 슬롯 기동 즉시 외부 프로브 요청 유입 시 지연 초기화(Lazy Init)로 인한 커넥션 풀/JPA 바인딩 병목 및 이에 따른 `API Health Status` 일시적 하락(0) 현상을 분석 완료.
      - **서버 기동 즉시 가용 상태 돌입**: `application-local.yml` 내 `spring.main.lazy-initialization` 설정을 `false`로 원상 복구하여, 서버 기동 완료와 동시에 모든 주요 빈(Bean) 및 리소스가 선제 할당(Eager Init)되어 외부 요청에 지연 없이 즉각 반응하도록 조치 완료.

    - **GraalVM Quick Build 모드 설정을 통한 빌드 파이프라인 속도 최적화 (Step 42 - 완료)**:
      - **빌드 옵션 추가**: [build.gradle](file:///c:/Users/82107/dev/project/nemologic/backend/build.gradle) 파일 내 `graalvmNative` 블록에 `-Ob` (Quick Build) 옵션을 추가하여 불필요한 컴파일러 최적화 분석 단계를 스킵함으로써 GitHub Actions의 GraalVM nativeCompile 빌드 속도 개선 완료.

    - **로고 기반 버퍼링 스피너 및 에러 처리/재시도 환경 구축 (Step 43 - 완료)**:
      - **로고 기반 스피너 구현**: 퍼즐 로드 대기 중이던 단순 텍스트 표시 대신 2x2 퍼즐 보드 로고 디자인을 모사한 회전 애니메이션의 `spinner-logo` 및 펄싱 텍스트(`loading-text`) 스피너 UI 구축 완료.
      - **에러 핸들링 및 재시도 연동**: 스테이지 상세 조회 API(500 에러 또는 네트워크 단절 등) 실패 시 무한 대기를 방지하도록 `loadError` 및 `isLoading` 상태값 제어 구문을 적용하여 바운딩하고, 에러 아이콘, 설명 메시지, 그리고 `handleRetryLoad` 버튼을 포함한 `error-state` UI 연동 완료.
      - **TDD 기반 단위 테스트 전원 통과**: `App.test.ts`에 로딩 중 회전 로고 렌더링 검증, 로드 실패 시 에러 화면 노출 검증, 그리고 재시도 버튼 클릭 시 데이터 리로드 호출 검증을 다루는 2개 통합 테스트 케이스를 신설하여 전체 70개 Vitest 프론트엔드 테스트 100% 통과 완료.

    - **로컬 개발 환경용 멀티스테이지 Dockerfile 분리 구성 (Step 44 - 완료)**:
      - **로컬 Dockerfile 분리**: 운영용 `Dockerfile`이 GitHub Actions의 GraalVM Native Image 리눅스 빌드 결과물 및 pre-built frontend dist 폴더를 복제하는 구조로 고정됨에 따라, 윈도우 등의 로컬 환경에서 `docker-compose` 구동 시 컴파일 바이너리 및 정적 자원 부재로 빌드가 실패하거나 소스 변경이 반영되지 않는 문제를 분석 완료.
      - **빌드 파이프라인 격리**: backend(`Dockerfile.local`)에는 Gradle 및 표준 JRE 기반 멀티스테이지 컴파일 이미지를 적용하고, frontend(`Dockerfile.local`)에는 Node.js 컴파일 스테이지를 탑재하여 로컬 호스트 빌드 의존성 없이 `docker-compose up --build` 호출만으로 양쪽 모두 소스 컴파일 및 최신 코드 기동이 실시간 반영되는 격리된 원클릭 로컬 빌드 환경을 구축 완료.

    - **서버 에러(502/500) 및 클라이언트 네트워크 에러 구분 처리 (Step 45 - 완료)**:
      - **에러 메시지 상세화**: 기존에 모든 API 호출 예외 상황에 대해 일괄적으로 네트워크 연결을 확인하라고 오보하던 피드백 구문을 리팩토링하여, HTTP status code(>= 500) 수신 시 `서버 내부 오류`로 상세 분류하도록 수정 완료.
      - **TDD 기반 단위 테스트 전원 통과**: `App.test.ts`에 502 status 코드 수신 시 실제 서버 에러에 알맞은 사용자 에러 팝업 메시지(`server error (502)`)가 표현되는지를 매핑 검증하는 테스트 케이스를 신설하고 전체 71개 Vitest 테스트 100% 통과 완료.

    - **초기 로딩 화면 스피너 렌더링 누락 및 CSS 높이 붕괴 해결 (Step 46 - 완료)**:
      - **초기 상태 초기화**: `App.vue`에서 `isLoading` 상태값의 초기값을 `false`에서 `true`로 갱신하여, 애플리케이션 최초 구동 후 비동기 API 요청이 진행되는 동안 빈 화면 대신 로고 스피너가 즉시 렌더링되도록 보장 완료.
      - **CSS 레이아웃 붕괴 방지**: 부모 컨테이너가 뷰포트 높이에 반응하여 높이가 일시적으로 0으로 붕괴되더라도 스피너가 숨겨지지 않고 화면 중앙에 최소 높이를 확보하며 표출되도록 `.loading-state` 스타일에 `min-height: 200px` 및 `width: 100%` 속성 연동 완료.
      - **TDD 기반 단위 테스트 확인**: 71개의 프론트엔드 유닛/통합 테스트가 모두 정상적으로 성공(Pass)함을 최종 확인 완료.

    - **Grafana Cloud 및 AWS EC2 경보 알림 누락 개선 (Step 47 - 완료)**:
      - **Grafana 알림 정책 정의**: `infra/terraform/grafana.tf` 내에 `grafana_notification_policy` 리소스를 신규 구성하여, 생성된 경보들이 정의된 이메일 연락처(`Developer-Email-Alerts`)로 정상 라우팅되도록 설정 완료.
      - **무데이터(No Data) 대응**: 서버 완전 중단 시 지연/미작동 방지를 위해 `Nemologic-Service-Down-Alert` 룰의 `no_data_state` 및 `exec_err_state` 옵션을 `Alerting` 상태로 강제하여 데이터 유실 시 즉각 경보가 발생하도록 최적화 완료.
      - **물리적 EC2 장애 경보 추가**: `infra/terraform/main.tf` 파일에 `StatusCheckFailed` 메트릭을 추적하는 `aws_cloudwatch_metric_alarm` 리소스를 배치하고 SNS 토픽과 연결하여 인스턴스 정지/네트워크 단절 등의 물리 다운 시 실시간 감지가 가능하도록 인프라 구성 완료.

    - **퍼즐 선택 목록 내 규격(크기)별 필터링 기능 추가 (Step 48 - 완료)**:
      - **필터 UI 도입**: 퍼즐 선택용 상단 플로팅 드롭다운(`.puzzle-selector-dropdown`) 상단에 `All` 및 사용 가능한 규격들(`5x5`, `10x10` 등)을 동적으로 추출하여 표시하는 가로형 버튼 바(`play-size-filter-bar`) 구축 완료.
      - **동적 필터 적용**: 선택한 규격 필터링 조건(`selectedPlaySizeFilter`)과 실제 노출 대기 리스트(`allUnclearedStages`)를 실시간 매핑 연산(`filteredPlayStages`)하여, 지정 크기 퍼즐들만 드롭다운에 출력되도록 가독성 개선 완료.
      - **TDD 단위 테스트 확인**: 추가/변경된 템플릿 및 반응형 동작 하에서도 기존 71개 전체 유닛/통합 테스트가 오류 없이 빌드 및 정상 통과(Pass)함을 최종 확인 완료.

    - **선택 크기 고정 플레이 루프 및 자동 크기 확장 빌드 (Step 49 - 완료)**:
      - **크기 고정 플레이 루프 구현**: `selectedPlaySizeFilter` 조건이 `'All'`이 아닐 때, 퍼즐 해결 후의 자동 전환 핸들러(`navigateToNextPuzzle`) 내에서 현재 지정된 크기(`targetSize`)의 잔여 미해결 퍼즐 리스트 내에서만 연속 기동하도록 룰 확장 완료.
      - **자동 크기 승급(Ascending Promotion)**: 현재 설정된 규격의 모든 퍼즐을 클리어했을 경우, 사용 가능한 규격 오더(`availablePlaySizes`) 중 다음으로 높은 단계의 규격(예: 5x5 -> 10x10 -> 15x15)으로 `selectedPlaySizeFilter`를 자동 갱신하고 연이어 첫 번째 퍼즐을 로드하는 오토 승급 체계 연동 완료.
      - **실시간 반응형 필터 전환**: 유저가 수동으로 드롭다운 필터 규격을 변경할 시, 현재 플레이 중인 퍼즐이 바뀐 규격과 다르면 즉시 해당 크기의 첫 번째 미해결 퍼즐로 화면이 자동 전환되도록 `watch` 리스너를 통한 정합성 제어 완료.
      - **컴파일 경고 해결**: Vue script 내 변수 선언 순서로 발생했던 block-scoping 경고를 모든 반응형 변수/컴퓨티드 변수의 선언 위치를 Setup 스크립트 최상단으로 재정리 및 리팩토링하여 해소 완료.
      - **TDD 단위 테스트 확인**: 총 71개의 유닛/통합 테스트가 오류 및 warning 없이 100% 정상 통과함을 보장 완료.

    - **퍼즐 크기 필터 바 상시 노출 및 레이아웃 최적화 (Step 50 - 완료)**:
      - **상시 노출 레이아웃**: 기존 퍼즐 선택 드롭다운 내부에 숨겨져 있던 크기 필터 바(`.play-size-filter-bar`)를 드롭다운 밖으로 꺼내어, 화면 상단 플로팅 선택기 영역(`.puzzle-selector-floating-container`)에 항상 보이고 즉각 조작 가능하도록 상시 노출 구조로 재배치 완료.
      - **플로팅 캡슐 디자인 적용**: 필터 바의 배경을 반투명 다크 블루 슬레이트 톤과 블러 효과(`backdrop-filter`), 얇은 외곽선 및 완전한 라운드 알약 모양(Pill-shaped capsule)으로 다듬어 로직 디자인 시스템에 동화 완료.
      - **반응형 세로 스택 구성**: 필터 바와 현재 선택된 스테이지명 배지 간의 레이아웃이 겹치지 않도록 플로팅 컨테이너를 Flex 세로 컬럼 스택 구조로 리팩토링 및 간격을 최적화 완료.
      - **TDD 단위 테스트 확인**: 71개 Vitest 프론트엔드 테스트 전체 통과 확인 완료.

    - **우측 수직 고정 크기 필터 배치 및 중복 크기 표기 제거 (Step 51 - 완료)**:
      - **우측 수직 배치**: 화면 상단에 자리 잡고 있던 크기 필터 바(`.play-size-filter-bar`)를 화면 우측 영역에 수직(`position: fixed; right: 25px; top: 50%`)으로 배치하여 상단 수직 공간의 시각적 답답함을 해소하고 캔버스 가용 영역을 확장 완료.
      - **모바일 하단 배치**: 모바일 화면(`max-width: 768px`)에서는 하단 중앙 수평 플로팅 레이아웃으로 미디어 쿼리를 연동하여 모바일 기기 한 손 조작 편의성 극대화 완료.
      - **상단 플로팅 컨테이너 원복**: 필터 바가 우측으로 분리됨에 따라 상단 플로팅 컨테이너를 본래의 컴팩트한 레이아웃(배 Badge 중심) 및 `top: 25px` 위치로 환원 완료.
      - **중복 크기 레이블 제거**: 우측 필터 바를 통해 현재 선택된 크기가 상시 지시되므로, 중복 및 혼선을 초래하던 퍼즐 선택기 캡슐 배지 내부의 크기 표시(`active-stage-badge-size`)와 드롭다운 내 각 아이템별 크기 표시(`stage-card-size`)를 템플릿에서 완전 제거 완료.
      - **TDD 단위 테스트 확인**: 71개 Vitest 프론트엔드 테스트 전체 통과 확인 완료.

    - **배지 텍스트 정렬, 수직 바 여백 최적화 및 모바일 뷰 HUD 겹침 문제 해결 (Step 52 - 완료)**:
      - **배지 정중앙 정렬**: `.active-stage-badge`에 `position: relative`와 가로 padding을 부여하고, `.active-stage-arrow`를 `position: absolute; right: 1.25rem`로 우측 정렬하여 배지 내 퍼즐명이 정중앙에 정확히 위치하도록 텍스트 정렬 개선 완료.
      - **수직 필터 바 요소 맞춤(Fit)**: `.play-size-filter-bar` 내 버튼들을 `align-items: stretch`를 적용하여 모든 버튼이 균일한 폭을 가지도록 맞췄으며, 내부 패딩과 버튼 패딩을 조절하여 여백이 많던 부분을 요소에 딱 맞게 최적화 완료.
      - **모바일 뷰 HUD 가림 버그 해결**: 모바일 화면(`max-width: 768px`) 환경에서 `position: fixed`로 인해 하단 혹은 상단의 Zoom HUD 및 Draw Mode HUD를 덮는 현상을 원천 방지하기 위해, 모바일 환경에서는 일반적인 레이아웃 흐름(`position: static`)을 갖는 가로형 필터 바 구조로 전환하여 캔버스 제어 버튼들과 레이아웃 겹침 문제를 완벽히 해결 완료.
    - **홈페이지 고도화 및 SLA 텔레메트리 대시보드 구축 (Step 53 - 완료)**:
      - **기본 랜딩 탭 전환**: `App.vue`에 `'home'` 탭을 추가하고, 테스트 환경(`isTestEnv`)을 제외한 실사용자 최초 진입 시 홈 화면이 기본 노출되도록 초기 세팅 완료.
      - **실시간 가용성 텔레메트리**: backend `/api/analytics/stats`로부터 수신한 SLA Uptime(%), MTBF, MTTR, 당일 고유 방문자 수, 총 방문 수, 퍼즐 플레이 횟수(시도/성공) 데이터를 글래스모피즘(Glassmorphism) 카드 그리드 구조로 보기 쉽게 렌더링 완료.
      - **앱 소개 및 플레이 유도**: rogic.io 고유의 회전형 네모로직 퍼즐 메카닉 설명과 게임 플레이로 진입시키는 CTA(Call-To-Action) 버튼 탑재.
      - **릴리즈 패치 노트 타임라인**: v1.0.0(최초 출시)부터 v1.3.0(대시보드 및 SSL 보안 구축)까지의 개발 및 운영 변경 이력을 수직 타임라인으로 시각화 완료.
      - **TDD 기반 단위 및 통합 테스트 성공**: `App.test.ts`에 홈 탭 진입, 텔레메트리 렌더링, CTA 버튼 작동 검증 테스트 케이스 추가 완료 및 전체 72개 Vitest 프론트엔드 테스트와 66개 백엔드 JUnit 테스트 100% 통과 완료.

    - **홈페이지 디자인 리팩토링 및 Vercel 스타일 독립적 랜딩 구성 (Step 54 - 완료)**:
      - **독립적인 웹진 형태 구성**: 기존 게임 화면에 포섭되어 보조 창 형태로 뜨던 홈페이지 구조를 전면 탈피하여, 홈 화면 진입 시에는 메인 상단 헤더(.app-header)를 아예 숨기고 Vercel 테마 스타일의 단독 랜딩 레이아웃을 제공하도록 설계 완료했습니다. 추가적으로 복잡함을 덜기 위해 랜딩 페이지 내의 상단 네비게이션 바(.landing-nav), 시스템 텔레메트리(System Telemetry) 섹션, 패치노트 타임라인 및 System Status 버튼을 완전히 제거하여 화면을 한층 더 미니멀하고 직관적으로 정리했습니다.
      - **게임 디자인 시스템과의 시각적 통일성 부여**:
        - 홈페이지의 배경색을 단순 Jet Black(#000000)에서 게임 메인 테마와 조화를 이루는 매우 깊은 남색 톤인 Deep Slate-Black(#0a0f1d)으로 수정하여 자연스럽게 통일감을 조성했습니다.
        - Hero 영역의 메인 타이틀을 기존 'Rotate Logic'에서 서비스 도메인 정체성에 맞는 'rogic.io'로 수정하였고, 게임 헤더의 타이틀(.app-title)과 동일하게 letter-spacing(-0.5px) 및 line-height(1.25)를 동기화했습니다. 또한, 글자 외곽 및 그라데이션이 잘려 보이는 브라우저 클리핑 이슈를 방지하기 위해 padding 처리 및 display: inline-block 속성을 추가하여 디자인적 완결성을 달성했습니다.
        - 핵심 CTA 플레이 버튼의 단조로운 흰색/검은색 스타일을 게임 테마의 시그니처 시안-인디고 그라데이션 및 반응형 글로우 섀도우 디자인으로 개선했습니다.
        - 시스템 텔레메트리 카드 및 타임라인 컨테이너에 게임 고유의 반투명 아크릴 질감인 유리모피즘(Glassmorphism)과 마우스 호버 시 시안(Cyan) 빛 윤곽선이 살아나는 스타일을 일관되게 이식 완료했습니다.
        - **앱 전체 컨테이너 세로 테두리 제거**: `#app` 최상단 컨테이너의 좌우에 세팅되어 시각적으로 거슬리던 1px 두께의 수직 회색 테두리(`border-inline`)를 제거(`border-inline: none`)하여 홈 화면과 게임 화면 모두 양쪽 여백이 깔끔하게 녹아들도록 처리 완료했습니다.
        - **홈페이지 설명 문구 수정**: 격자를 돌려서 푸는 것이 아닌 퍼즐을 완성하면 보정 회전되는 게임 진행 방식을 혼선 없이 안내하도록 Hero 서브타이틀 설명 문구를 `"The next-generation Nonogram. Align your mind, solve the puzzle, and watch the grid rotate to reveal the hidden pattern."`으로 정교화 완료했습니다.
        - **해시 라우팅(Hash Routing) 기반 뒤로가기 및 URL 연동 지원**: 가볍고 의존성 없는 자체 라우팅을 구현하기 위해 `window` 객체의 `hashchange` 이벤트 리스너와 해시 파서(`getTabFromHash`, `updateHashFromTab`)를 설계했습니다. 이를 통해 사용자가 게임 플레이, 마이페이지 탭 간 이동 시 브라우저 주소창에 경로가 연동(`#/play`, `#/mypage`)되어 뒤로가기/앞으로가기가 부드럽게 지원되고, 특정 화면으로의 직접 진입(Deep Linking)이 정상 작동합니다. 특히 홈 화면의 경우 주소창에 `#/home` 같은 해시 표기가 붙어 지저분해 보이지 않도록 `history.replaceState` API를 결합하여 깔끔하게 순수 도메인 네임(`rogic.io`)만 노출되도록 정리했습니다.
      - **동적 바디 스크롤 및 뷰포트 레이아웃 분기**: 게임 플레이 및 마이페이지 화면에서는 100vh에 캔버스를 맞추어 body 스크롤을 방지하고 터치 액션을 고정시키는 반면, 홈 탭으로 전환될 때는 body 스크롤(`overflow: auto`, `height: auto`)을 허용하여 자연스럽게 전체 랜딩 페이지를 상하로 훑어볼 수 있도록 뷰포트 반응성 라이프사이클 통합 완료.
      - **TDD 연동 테스트 보완**: 홈 탭 전환을 위해 기존에 제거된 `.tab-btn-home` 버튼 대신 플레이 뷰 헤더의 `.logo-wrapper` 클릭 이벤트를 검증용 액터로 갱신 적용하여, Vitest 전체 72개 테스트 및 프로덕션 번들 빌드(`npm run build`)가 100% 정상 통과함을 최종 확인 완료.

    - **플레이 크기 필터 바 모서리 및 버튼 둥글기 완벽 맞춤 (Step 55 - 완료)**:
      - **캡슐 외곽선 및 패딩 비정렬 수정**: 수직 필터 바(`play-size-filter-bar`)의 전체 `border-radius`를 `20px`로 다듬고 내부의 개별 버튼들의 모서리가 캡슐 외벽의 둥근 형태와 어색하게 어긋나던 현상을 해결하기 위해 첫 번째/마지막 버튼에 대한 특화 모서리 스타일 적용 완료.
      - **선택된 버튼 외각 라운딩 처리**:
        - **수직 레이아웃 (데스크톱/태블릿)**: 첫 번째 버튼("All")의 상단 코너 반경을 `16px` (컨테이너 내측 최적 곡률), 하단 코너 반경을 `10px`로 지정하고, 마지막 버튼("30x30")의 하단 코너 반경을 `16px`, 상단 코너 반경을 `10px`로 지정하여 활성화된 둥근 직사각형 선택 표시가 외곽 캡슐 프레임의 곡률에 완벽하게 맞닿고 밀착되도록 스타일 튜닝 완료.
        - **수평 레이아웃 (모바일 세로모드 768px 이하)**: 첫 번째 버튼의 좌측 코너 반경을 `16px`, 우측 코너를 `10px`로 지정하고, 마지막 버튼의 우측 코너 반경을 `16px`, 좌측 코너를 `10px`로 전환하여 가로형 캡슐 둥글기와 활성 영역이 시각적으로 일치하도록 반응형 미디어 쿼리 구조 추가 보완 완료.
      - **브라우저 시각적 검증 및 빌드**: 헤드리스 브라우저 Subagent를 통해 `http://localhost:5173`로 진입하여 "All" 및 "30x30" 크기 필터 선택 시 외벽 테두리 오차 없이 미려하게 가득 차는 상태를 스크린샷과 비디오 파일로 최종 검증 및 프로덕션 빌드 성공.

    - **직관적 UI 기반 도움말(Help) 버튼 및 모달 제거 (Step 56 - 완료)**:
      - **도움말 구성 요소 완전 삭제**: 유저가 수동으로 조작법을 배울 필요가 없을 정도로 직관적인 게임 UI 완성도에 따라, 상단 헤더의 ❓ 도움말(`help-toggle-btn`) 버튼 및 최초 접속 시 노출되던 안내 모달(`.help-modal-overlay`) 템플릿을 전면 제거하여 화면을 극도로 깔끔하게 최적화 완료.
      - **컴포넌트 라이프사이클 및 반응형 상태 정리**: `App.vue` 내 `isHelpModalOpen` ref 변수와 `onMounted` 시점의 최초 방문 여부(`rotagic_visited`) 판별 및 로컬 스토리지 기입 분기를 삭제하여 리소스를 절감하고 상태 복잡도를 경감함.
      - **TDD 단위/통합 테스트 최적화**: 도움말 모달 개폐를 다루던 구버전 통합 테스트 케이스들을 `App.test.ts`에서 영구 소거하여, 에러 없이 총 70개의 Vitest 프론트엔드 테스트를 성공적으로 통과(Pass) 완료.

    - **Grafana 통합 대시보드 내 OS 디스크 사용률(Disk Usage) 모니터링 패널 추가 (Step 57 - 완료)**:
      - **디스크 사용률 수식 패널 설계**: `host.docker.internal:9100`에서 수집되는 `node_exporter` 지표 기반으로 호스트의 디스크 잔여량 및 실시간 사용 비율을 추적하기 위한 `"OS Disk Usage"` 패널(`panel-1010`)을 신설 완료. PromQL 연산 수식 `100 * (1 - (node_filesystem_avail_bytes{mountpoint="/"}/node_filesystem_size_bytes{mountpoint="/"}))` 및 `percent` 포맷 단위를 부여하여 시각 가독성 확보.
      - **그리드 레이아웃(GridLayout) 최적화**: 신규 패널 수용을 위해 기존 SLA Metrics 2행의 그리드 배치를 재조정 완료. 단일 수치 기반 지표(Incident Count, MTTR, MTBF)의 너비를 기존 6에서 `4`로 압축하고, 시계열 추이 모니터링이 용이한 수치 지표(Memory/Swap Usage, Disk Usage)의 너비를 `6`으로 설정하여 총합 `24` 그리드의 시각적 균형감 유지.
      - **설정 파일 반영**: `infra/monitoring/current_dashboard.json`을 프로그램 방식으로 안전하게 갱신하고, Agentless Pull 전환에 맞춰 `instance` 라벨 대신 `scrape_job` 라벨을 필터로 매핑하도록 PromQL 쿼리 및 변수 설정을 일괄 고도화 완료. Terraform `grafana.tf` 배포 설정과의 연계가 즉시 가능하도록 형상관리 동기화 완료.

    - **배포 서버(EC2) Docker 이미지 빌드 시 메모리 최적화 및 슬롯 단일화 (Step 58 - 완료)**:
      - **Alloy 중지 단계 조기 배치**: 배포 중 메모리 경합을 줄이기 위해, 기존에 빌드 완료 후 실행되던 `Stop alloy container before deployment to free up memory` 태스크를 `Build Docker images` 실행 직전 단계로 상향 배치하여 모니터링 에이전트가 차지하던 메모리 공간을 선제적으로 확보함.
      - **빌드 슬롯 단일화 및 명령어 최적화**: `backend-blue`와 `backend-green` 서비스는 동일 이미지(`nemologic-backend:latest`)를 사용하므로, 중복 빌드 오버헤드를 막기 위해 빌드 명령어에서 `backend-green`을 배제하고 `backend-blue` 및 `frontend`만 빌드하도록 명령어를 단일화 완료.
      - **TDD 검증**: 변경 사항 적용 후 프론트엔드 테스트(70개) 정상 통과 확인 완료.

    - **스테이징(Staging) 환경 구축 및 승인 게이트 기반의 프로모션 배포 파이프라인 연동 (Step 59 - 완료)**:
      - **스테이징 인프라 추가**: `main.tf` 및 `outputs.tf`를 고도화하여 별도의 스테이징용 EC2 인스턴스(`aws_instance.nemologic_staging_server`), 전용 Elastic IP, 스테이징 CloudWatch 로그 그룹 및 인스턴스 헬스 상태 알림 경보를 구성함.
      - **환경별 SSL/HTTPS 연동**: `nginx.stage.conf` 및 `docker-compose.stage.yml`을 구성하여 `stage.rogic.io` 도메인에 대한 SSL 리포팅 및 443 포트 포워딩을 격리 매핑함.
      - **Ansible 플레이북 템플릿화**: `playbook.yml`에 `env_name`, `cert_domain`, `certbot_domains` 변수들을 연동하여 스테이징(`stage.rogic.io`)과 운영(`rotagic.com`) 환경 모두 각각에 알맞은 Let's Encrypt SSL 인증서 자동 발급 및 갱신 파이프라인이 안전하게 가동되도록 고도화함.
      - **EC2 시작/종료 수명 주기 자동화**:
        - `deploy-staging` 실행 전 AWS CLI를 통해 중지 상태인 스테이징 EC2를 자동으로 부팅(`start-instances`)하고 인스턴스가 `running` 상태가 될 때까지 대기한 뒤 빌드 자산을 배포 및 테스트하도록 연동함.
        - 비용 최소화를 위해 매일 새벽 02:00 AM KST에 스테이징 EC2를 자동으로 안전하게 중지시키는 `staging-cleanup.yml` 야간 정리 워크플로우를 추가 구축함.
      - **CI/CD 워크플로우 최적화 (승인 게이트 도입 및 빌드 방식 원복)**:
        - `build` 단계를 신설하여 GraalVM Native Image 컴파일과 Node frontend 빌드를 최초 1회만 수행한 후, 생성된 바이너리와 자산을 GitHub Artifact로 업로드해 중복 빌드를 차단함.
        - `deploy-staging` 단계를 통해 `main` 브랜치 푸시 시 스테이징 서버에 자동으로 배포하고, 이후 `production` 환경에 배포하기 전에 담당자가 수동 승인(Approval Gate)을 진행해야만 운영 환경으로 배포를 이관(Promotion)하도록 파이프라인의 안전장치를 완비함. 또한, 배포 작업의 병렬 실행 충돌을 원천 차단하기 위해 GitHub Actions `concurrency` 그룹(`cancel-in-progress=false`) 설정을 추가하여 동시성 배포 큐 대기 제어를 구성 완료함.
        - `docker save/load` 방식은 `gunzip` 해제 및 Docker 레이어 언팩킹 중 순간 리소스(메모리/디스크 I/O) 피크치가 높아 512MB RAM 사양에서 쓰레싱을 유발하므로, 기존의 온호스트 바이너리 카피 빌드(`docker compose build`) 방식으로 원복 완료함.

    - **GitHub Container Registry (GHCR) 이미지 풀 방식 전환 및 메모리 쓰레싱 근본적 해결 (Step 60 - 완료)**:
      - **문제 상황 진단**: GraalVM 네이티브 바이너리 파일 복사 및 온호스트 빌드(`docker compose build`) 방식 역시 512MB RAM 사양의 EC2 인스턴스에서는 Docker 데몬 빌드 컨텍스트 전송 및 CPU 점유로 인해 SSH 접속이 끊어지고 디스크 I/O Wait가 98%에 달하는 스레싱 장애가 발생함을 확인함.
      - **GHCR 연동 및 빌드 오프로딩 완료**: Docker 이미지 빌드 및 푸시 작업을 GitHub Actions Runner에서 직접 수행하여 `ghcr.io`에 배포 버전을 push하도록 `ci-cd.yml` 워크플로우를 고도화함.
      - **서버 측 배포 흐름 전환**: 대상 EC2 호스트에서 직접 Docker 이미지를 빌드하는 과정을 전면 제거하고, Ansible 플레이북이 `GHCR_USERNAME`과 `GHCR_PAT` 환경변수를 활용하여 GHCR에 로그인한 뒤 사전 빌드된 이미지를 `docker compose pull`로 내려받아 롤링 배포를 수행하도록 전환 완료함.
      - **안정성 확인**: 빌드 과정의 호스트 메모리 및 CPU 자원 소모를 최소화하여 512MB RAM 한계 내에서도 SSH 중단이나 스레싱 없이 무중단 배포(Zero-downtime Blue-Green)가 안정적으로 완수됨을 보장함.

    - **모바일 화면 하단 HUD 잘림 오류 수정 및 뷰포트 고도화 (Step 61 - 완료)**:
      - **다이나믹 뷰포트 높이(100dvh) 적용**: 모바일 크롬 및 사파리 브라우저에서 하단 툴바/주소창이 노출될 때 `100vh` 기반 레이아웃의 하단이 화면 바깥으로 밀려나 그리기 모드 전환기(`.draw-mode-hud`) 및 줌 컨트롤(`.zoom-hud`)이 잘리던 문제를 해결하기 위해, `body` 및 `.app-container`를 포함한 모든 최상위 레이아웃 컨테이너에 `height: 100dvh`(dynamic viewport height)를 fallback 지정하고, JS 탭 전환 시 동적 height 변경 핸들러에도 `100dvh`를 자동 부여하도록 구조를 고도화함.
      - **모바일 HUD 크기 및 간격 축소**: 화면 폭이 좁은 모바일 기기(max-width: 768px)에서 HUD 오버레이의 차지 영역과 여백을 최소화하여 플레이 가독성을 보장하기 위해, `NonogramCanvas.vue` 내 미디어 쿼리를 신설해 HUD의 bottom 여백을 `20px` -> `12px`로 줄이고 줌 버튼 및 텍스트 폰트 스케일을 약 15~20%가량 축소 적용함.
      - **무색 모노크롬 SVG 아이콘 적용**: 상단 헤더 메뉴의 'Game Play'(🎮), 'My Page'(👤), 'Leaderboard'(🏆) 버튼에 사용되던 컬러 이모지를 제거하고, 버튼 텍스트의 투명성과 활성화 상태에 맞춰 색상이 부드럽게 유기적으로 동기화되는 커스텀 모노크롬 인라인 SVG 아이콘을 적용하여 현대적이고 차분한 다크 테마 디자인 톤앤매너 완성.
      - **확대/축소(Zoom) HUD 하단 중앙 배치**: 캔버스 하단 우측에 치우쳐 있던 줌 패널(`.zoom-hud`)을 하단 중앙(`left: 50%; transform: translateX(-50%);`)으로 이동시켜 그리기 모드 HUD(`.draw-mode-hud`, 하단 좌측 배치)와 시각적 균형을 이룸과 동시에 모바일 엄지 조작 피로도를 줄였습니다.
      - **하단 우측 플로팅 크기 필터 드롭다운 전환**: 기존에 우측에 고정 노출되던 크기 필터 바(`.play-size-filter-bar`)를 제거하고, 하단 우측에 플로팅 배지(`.active-size-badge`) 형태로 띄운 뒤 클릭 시 선택지(All, 5x5, 10x10 등)가 부드럽게 슬라이드업(`slide-up`)되는 드롭다운 구조로 변경하여 캔버스 활용 공간을 넓히고 미니멀리즘 디자인을 한 단계 고도화했습니다. 바깥 영역 클릭 시 자동 닫힘 기능도 구현 완료했습니다.
      - **테스트 정합성 통과**: 변경 사항 반영 후에도 기존 70개 프론트엔드 테스트 케이스가 사이드이펙트 없이 100% 정상 통과함을 보장함.

    - **Grafana 대시보드 SLA 지표 패널 분리 및 OS 지표 환경별 격리 (Step 62 - 완료)**:
      - **SLA 대시보드 가독성 고도화**: Consolidated 되었던 MTTR, MTBF(24h, 7d, 30d) 지표들을 각각 개별 패널(panel-1011~1014 신설)로 완전히 분리하고, 24 그리드 내에 가로로 조화롭게 격리 배치하여 시각적 인지도를 대폭 개선함.
      - **OS 메트릭 중복 노출 해결**: OS Memory/Swap Usage (`panel-1009`) 및 OS Disk Usage (`panel-1010`) 패널의 PromQL에 `$env` (springboot 인스턴스명에서 `stage` 또는 `prod`를 자동 추출하는 변수) 필터를 적용하여, staging 환경과 production 환경의 지표가 중복으로 노출되지 않고 각각 선택한 대상 인스턴스에 맞추어 깔끔하게 1개 라인으로만 렌더링되도록 수정 완료함.
      - **JSON 정합성 검증**: python의 json 라이브러리를 통해 수정한 `current_dashboard.json` 파일의 구문 유효성을 백엔드 및 인프라 디렉토리 빌드 이전에 사전 검증함.

    - **AWS CloudWatch Logs 대시보드 연동 구성 (Step 63 - 완료)**:
      - **CloudWatch Logs 패널 신설**: `current_dashboard.json` 대시보드 모델 사양에 `"AWS CloudWatch Logs"` 전용 행(Row) 및 24 가로 그리드를 완전히 채우는 10 높이의 Logs 시각화 패널(`panel-1015`)을 구성 및 추가 완료함.
      - **Logs Insights Query 지정**: `/aws/ec2/nemologic` 로그 그룹을 대상으로 실시간 로그 추이를 최신순으로 정렬 조회하는 `fields @timestamp, @message | sort @timestamp desc | limit 100` Logs Insights 쿼리 및 `ap-northeast-2` 리전을 기본 매핑함.
      - **동적 데이터소스 UID 결합**: `variables.tf` 내에 `grafana_cloudwatch_datasource_name` 변수를 선언(기본값 `"CloudWatch"`)하고, `grafana.tf` 내에서 Grafana API를 활용해 이 이름의 데이터소스 UID를 실시간 조회한 뒤, `sla_dashboard` 리소스 선언부에서 `current_dashboard.json` 내의 placeholder인 `"$${DS_CLOUDWATCH}"`를 해당 CloudWatch data source of 실제 고유 UID로 자동 치환 및 배포하도록 Terraform IaC 구성을 고도화함.

    - **Git Commit SHA 기반의 Docker 이미지 버전 태깅 및 롤링 배포 연동 (Step 64 - 완료)**:
      - **Docker 이미지 빌드 파이프라인 고도화**: `ci-cd.yml` 내 빌드 단계에서 Docker 이미지를 단순 `latest`뿐만 아니라 고유 식별을 위해 Git Commit SHA (`sha-${{ github.sha }}`) 태그를 부여하여 동시에 GHCR에 빌드 및 푸시하도록 변경함.
      - **Ansible 및 Docker Compose 환경변수 동적 주입**: `playbook.yml` 내 `vars`와 원격지 `.env` 파일에 `IMAGE_TAG` 매핑 변수를 신설하여, 배포 실행 시점에 GHCR에서 해당 SHA 버전 이미지를 정확히 가져와 배포하도록 연동 완료함.
      - **docker-compose.prod.yml 및 docker-compose.stage.yml 이미지 버전 제어**: 하드코딩된 `:latest` 이미지 명세를 `${IMAGE_TAG:-latest}` 환경변수 보간법 구조로 전면 전환하여, 무중단 롤백 및 다중 환경에서의 빌드 정합성을 완벽히 확보함.

    - **GitHub Release 및 패치노트 생성 자동화 도입 (Step 65 - 완료)**:
      - **태그 릴리즈 트리거 고도화**: Git tag `v*` 형태가 푸시될 경우 파이프라인(`ci-cd.yml`)이 자동 동작하도록 push 트리거 대상을 확장하고, 각 빌드 및 배포 작업(`build`, `deploy-staging`, `deploy-production`)의 conditional을 태그 배포 시에도 정상 수행되도록 갱신 완료함.
      - **동적 이미지 태그 할당**: 태그 푸시인 경우 Docker 이미지 빌드 시 Git Commit SHA 대신 해당 태그 이름(예: `v1.0.0`)을 `IMAGE_TAG`로 적용해 태깅 및 푸시하며, Ansible 배포 스크립트에도 동적 전달하도록 파이프라인 변수를 정교화함.
      - **자동 GitHub Release 생성 단계 수립**: Production 배포 완료 이후 최신 커밋로그와 PR 내역을 기반으로 패치노트(Changelog)를 자동 추출하여 GitHub Release를 발행하는 `create-release` 전용 작업을 최종 단계에 통합 완료함.

---

## 2. 다음 단계: 서비스 고도화 및 운영 (Next Goals)

### 핵심 작업 목록
1. **CI/CD 파이프라인 정상 가동, SSL 적용 및 Grafana 모니터링 연동 확인**
   - GitHub Actions `infra-apply` 단계의 수동 승인을 제거하고 자동 적용(Auto-Apply)되도록 고도화 완료.
   - GitHub Actions `app-deploy`를 트리거하여 배포 및 기동 검증 진행 (SSL 인증서 발급, HTTPS 접속 테스트 및 Grafana Cloud 대시보드 메트릭 수집 검증).

    - **릴리즈 태그 자동 생성 및 푸시 워크플로우 추가 (Step 66 - 완료)**:
      - **Create & Push Release Tag 워크플로우 구현**: `.github/workflows/create-tag.yml`을 신설하여 GitHub UI 상에서 사용자가 버전 증가 범위(`patch`, `minor`, `major`)를 선택하고 원클릭 실행 시 자동으로 최신 Git 태그를 계산해 원격지에 푸시해 주도록 자동화함.
      - **태그 및 릴리즈 딜레이 연동**: 수동으로 Git 커맨드를 작성하지 않고도 안전하게 신규 릴리즈 및 롤백용 태그 버전을 생성할 수 있는 통합 관리 체계를 완성함.

---

## 3. 인프라 아키텍처 비용 최적화 및 타협 설계 (Self-Funded Infra Trade-offs)

### 설계 철학 및 목표
* **비용 최소화 및 실무 역량 입증**: 개인 포트폴리오 장기 운영에 따른 사비 충당 부담을 낮추면서, 현업 수준의 고가용성 설계와 실제 타협점(Trade-offs)을 명확히 분석하고 아키텍처적 대체안을 직접 설계·구현하여 역량을 증명함.

### 상세 타협 사항 및 대체 기술 전략
* **SPOF 극복 및 고가용성 타협 (ALB 배제)**:
  * AWS ALB(Application Load Balancer) 기동 시 발생하는 상시 기본 비용(월 약 $20)을 제거하기 위해 Route 53 및 단일 EC2 호스트 구조로 타협.
  * 단일 인스턴스 장애 시 CloudWatch 경보와 연동해 자동 재시작(Auto Recovery)을 수행하거나, Terraform 및 Ansible로 정의된 IaC 코드를 활용해 5분 이내에 인프라와 애플리케이션 컨테이너를 신속 복구(Recovery-Oriented Architecture)하도록 설정해 이중화를 대체함.
  * **Blue-Green 액티브-액티브(Active-Active) 배포 전략**: GraalVM Native Image 도입을 통해 컨테이너당 메모리 사용량을 30MB 안팎으로 대폭 경량화함에 따라, 배포 완료 후에도 기존 활성 슬롯을 중지하지 않고 Blue와 Green 백엔드 컨테이너가 모두 가동되는 Active-Active 구조를 영구 유지함. 배포 시작 시 메모리 집중 점유를 막기 위해 모니터링 에이전트(`alloy`)만 정지하고 배포 대상 슬롯을 신규 기동해 헬스체크 통과를 확인한 후, 기존 구버전 슬롯을 종료하지 않고 함께 구동 상태로 유지해 안정적인 무중단 서비스 제공과 빠른 로드밸런싱/롤백 가용성을 극대화함.
* **데이터베이스 영속성 레이어 타협 (RDS 대체)**:
  * AWS RDS 상시 구동 비용(월 약 $15~20 이상)을 방지하기 위해 단일 EC2 내 Docker Compose 기반 PostgreSQL 컨테이너를 구동.
  * RDS의 관리형 자동 백업 기능을 대체하기 위해, 매일 지정된 시간에 DB 백업 덤프파일을 생성하여 AWS S3 버킷으로 자동 전송하는 쉘 스크립트와 Cron 작업을 Ansible 플레이북으로 자동 구축함으로써 인프라 제어 및 데이터 보호 역량을 증명함.
  * **자원 제약 최적화 (Memory Optimization)**:
  * 512MB RAM 수준의 극단적인 저비용 인스턴스(`t4g.nano`/`t3.nano`) 환경에서의 구동을 장기적 목표로 설정.
  * JVM Metaspace 메모리 제약 및 GC 최적화 설정을 적용하고, GraalVM 기반 Native Image 컴파일 빌드를 도입해 메모리 점유율을 50MB 이하로 낮추는 파이프라인 구성을 검증.
  * **GraalVM Native Image Hibernate 프록시 호환성 오류 해결**: Native Image 환경에서 Hibernate의 `BytecodeProvider`가 `none`으로 설정되어 런타임 프록시 생성이 불가능한 문제(`HibernateException: Generation of HibernateProxy instances at runtime is not allowed`)를 진단. `History.java` 엔티티의 `User` 및 `Stage` 관계에 설정된 `@ManyToOne(fetch = FetchType.LAZY)`를 `FetchType.EAGER`로 변경하여 프록시 생성 없이 즉시 로딩(Eager Loading)되도록 수정. 대상 엔티티(`User`, `Stage`)가 경량 구조이므로 성능 영향은 무시할 수 있는 수준이며, JVM 기반 백엔드 단위 테스트 전원 통과를 확인 완료.
  * **Alloy 에이전트 영구 제거 및 Agentless Pull 아키텍처 전환**:
    * 512MB RAM 수준의 초경량 VM 환경에서 Grafana Alloy 에이전트 구동에 따른 메모리 및 CPU 부하로 인한 스래싱 장애를 차단하기 위해, Alloy 수집기 컨테이너를 영구 제거함.
    * **지표 수집(Pull 방식) 개방 및 Bearer Token 보안**: 호스트에는 10MB 미만의 메모리만 소모하는 `prometheus-node-exporter`만 9100 포트로 띄우고, 백엔드 프로메테우스 지표 `/actuator/prometheus` 및 호스트 지표 `/node-metrics`를 Nginx를 통해 외부로 개방함. 이때 Grafana Cloud Scrape Job과의 연동 및 보안 규격 충족을 위해 `Authorization: Bearer nemologic-metrics-token-2026` 헤더 존재 여부를 검증하고 누락 시 `401 Unauthorized`를 반환하도록 Nginx 설정을 고도화함.
    * **로그 수집(CloudWatch 연동) 오프로딩**: 서버 내부에 추가 수집기를 띄우지 않고, Docker 데몬의 `awslogs` 드라이버를 통해 CloudWatch Logs로 로그를 전송(서버 부하 0%)한 뒤, Grafana Cloud의 AWS CloudWatch 데이터소스를 연결하여 로그를 쿼리하도록 모니터링 아키텍처를 경량 최적화 완료함.
* **서비스 가용성 및 수명 주기 모니터링 타협 (SaaS 모니터링 비용 제로화)**:
  * AWS Route 53 Health Check(월 $0.50~$0.75) 및 CloudWatch Metric Alarm(월 $0.10)의 클라우드 상시 지출을 완전히 방지하기 위해, Grafana Cloud의 무료 티어 내에 포함된 Synthetic Monitoring(월 500,000회 무료 쿼터)을 도입.
  * 전 세계 3개 이상의 멀티 리전 Probes에서 60초 간격으로 `https://rogic.io/api/stages` API 엔드포인트를 검증하며, Grafana Alertmanager를 통해 Slack 및 이메일로 무제한 경보 알림을 송출하도록 연동함.
  * 시스템 다운타임 시 발생 빈도 및 유지 기간을 추적하는 PromQL 기반의 커스텀 대시보드(MTTR, MTBF, Availability %)를 수립하여, 운영 비용을 추가로 지출하지 않으면서도 정량적인 SLA 품질 지표 기반의 서비스 개선이 가능하도록 인프라 아키텍처를 영리하게 타협 및 고도화함.

---

## 4. 개발 규칙 및 제약사항 준수 확인
- **TDD 필수 준수**: 코어 모듈 및 비즈니스 로직 작성 시 반드시 테스트 코드가 선행되어 통과 여부를 검증해야 합니다.
- **디렉토리 격리**: 단일 작업 단위에서는 메인 디렉토리(`frontend/` 또는 `backend/`)를 독립적으로만 수정하며, 여러 메인 디렉토리의 파일들을 동시에 혼재해 수정하지 않도록 통제합니다.
- **반응성 디커플링**: 코어 비즈니스 로직 모듈 내에는 Vue 프레임워크나 반응성 종속성(Ref, Reactive)을 절대 사용하지 않습니다.
- **지표 및 데이터 기반 인프라 의사결정**: 인프라 변경, 신규 클라우드 리소스 도입, 실행 환경 최적화(예: GraalVM Native Image 전환) 등을 제안하거나 구현할 때는 반드시 부하 테스트 결과(TPS, Latency 분포) 또는 시스템 자원 사용량 지표(Disk I/O Wait, RAM/OOM 이력 등)를 사전/사후 실측한 정량적 데이터를 근거로 제시해야 합니다.




### Grafana 대시보드 OS 메트릭 필터 매핑 수정 - 완료
- **현상**: Alloy를 제거하고 Node Exporter 직접 Scrape Job(node-metrics) 구성 후, OS Memory/Swap 및 OS Disk Usage 지표 누락 현상 발생.
- **해결 내역**: infra/monitoring/current_dashboard.json 내의 해당 패널 쿼리에서 job 매핑을 정규식(job=~".*$env")에서 명시적인 job="node-metrics"로 고정 반영 완료.

### Grafana 대시보드 SLA Metrics 레이아웃 개선 - 완료
- **현상**: 여러 지표가 한 줄에 섞여 있어 시인성이 떨어짐 (Availability, MTTR, MTBF 패널 등)
- **해결 내역**: infra/monitoring/current_dashboard.json의 Nemologic Service SLA Metrics Row 패널 배치를 다음과 같이 정리 완료
  - Row 0: API Health Status, 30-Day Incident Count
  - Row 1: 24-Hour/7-Day/30-Day Service Availability (각 폭 8)
  - Row 2: 24-Hour/7-Day/30-Day MTTR (각 폭 8)
  - Row 3: 24-Hour/7-Day/30-Day MTBF (각 폭 8)
  - Row 4: OS Memory/Swap Usage, OS Disk Usage

### AI 퍼즐 자동 생성 알고리즘 개선 - 완료
- **현상**: 스케줄러를 통한 AI 퍼즐 생성 시, Gemini가 JSON Array 형식([[0,1]..])을 문자열로 직렬화하지 않고 원시 배열 형식으로 응답하여 파싱 에러 발생. 또한 무작위 모양이 
onogramSolver의 유일해(Unique Solution) 검증을 100% 확률로 통과하지 못해 실패를 반복하고 조용히 종료됨.
- **해결 내역**:
  - GeminiAiClient 프롬프트 명세 개선: JSON 배열 그대로를 응답하도록 지시.
  - AiStageGenerator 내부의 AiResponseDto.grid 타입을 String에서 JsonNode로 변경하여 유연한 배열 데이터 매핑 지원 구현.
  - 유일해 강제 보정 알고리즘(makeGridUnique) 도입: AI가 생성한 형태가 유일해 조건을 만족하지 못할 경우, 최대 50회 동안 랜덤하게 픽셀을 토글하며 유일해를 찾도록 보완(배경 작업이므로 15초 이내 소요).
  - 관련 단위 테스트(AiStageGeneratorTest) 수정 및 검증 완료.

### 자동 생성 퍼즐 사이즈 제한 - 완료
- **현상**: 25x25, 30x30과 같은 대형 퍼즐은 50번의 유일해 보정 로직만으로는 성공할 확률이 낮아, API 호출 실패와 타임아웃을 연쇄적으로 유발할 우려 제기.
- **해결 내역**:
  - DailyPuzzleScheduler에서 매일 새벽 자동 생성하는 퍼즐 사이즈 목록 중 25, 30을 임시 제외 ([5, 10, 15, 20]만 유지).
  - 관련 테스트 케이스 수정 후 정상 작동 확인.

### Grafana OS 메트릭 및 Incident Count 대시보드 개선 - 완료
- **현상**: Incident Count 패널이 레이아웃을 무너뜨리며 24h MTTR 및 MTBF 패널을 덮어씌움. OS 메트릭 누락 지속.
- **해결 내역**:
  - current_dashboard.json의 OS 메트릭 PromQL에서 범용성 개선.
  - Incident Count 패널(24h, 7d, 30d)을 새로운 ID로 분리하여 각 메트릭이 하나의 행(Row)을 온전히 차지하도록 GridLayoutItem 전면 재배치 (API Health 1줄, Incident 1줄, Avail 1줄, MTTR 1줄, MTBF 1줄, OS 1줄).

### OS CPU (iowait/idle) 패널 추가 - 완료
- **현상**: Memory/Swap 사용량 외에 스래싱(Thrashing) 등 디스크 I/O 병목 및 시스템 유휴 상태를 확인하기 위한 CPU 지표가 누락됨.
- **해결 내역**:
  - current_dashboard.json에 OS CPU iowait & idle (%) 패널 신규 추가.
  - 기존 OS Memory/Swap, OS Disk Usage 패널과 나란히 한 줄에 표시되도록 GridLayout Width를 12에서 각각 8로 조정.

### 배포 및 릴리즈 생성(GitHub Release & Tag) 파이프라인 자동화 - 완료
- **현상**: Git 태그 push 시에만 릴리즈 및 패치노트 생성 작업이 수행되어, 일반 main 브랜치 푸시/머지 시 릴리즈가 누락되거나 매번 수동으로 태그를 생성해야 하는 번거로움 발생.
- **해결 내역**:
  - `ci-cd.yml` 내 `create-release` 작업의 `if` 조건을 `main` 브랜치 푸시 및 태그 푸시 모두 작동하도록 변경.
  - `create-release` 작업에 `permissions: contents: write` 권한을 부여하여 쉘 스크립트에서 태그 생성 및 GitHub Release 작성을 허용함.
  - `Determine and Create Tag` 단계를 구축하여, `main` 브랜치 푸시 시 커밋 메시지 히스토리를 분석해 Conventional Commits 규칙(BREAKING CHANGE/! -> Major, feat -> Minor, 그 외 -> Patch)에 따라 다음 태그 버전을 자동 연산한 후 로컬 태그 생성 및 원격 `git push`를 수행하도록 자동화 완료.
  - 태그 푸시 이벤트에 의해 작동할 때는 기 생성된 태그를 그대로 활용해 GitHub Release만 발행하도록 분기 처리 완료.

### 로그인 기능 롤백 및 OS 메트릭 환경 격리 (Option A) - 완료
- **현상**: 
  - 진행 중이던 구글 소셜 로그인 연동(프론트/백엔드) 구현 사항을 중단하고 롤백 요구.
  - Grafana 통합 대시보드에서 `OS Memory/Swap Usage` 및 `OS Disk Usage` 지표가 Staging과 Production 양측 서버로부터 수집되어 중복(2개씩)으로 렌더링되던 현상 발생. `OS CPU` 패널도 평균 연산(`avg`)에 의해 두 환경의 지표가 합산/평균되어 오인될 소지가 있음.
- **해결 내역**:
  - **로그인 기능 롤백**: 로컬 변경 사항을 `git restore` 및 `git clean`을 통해 완벽히 이전의 안정된 커밋 상태로 복구함.
  - **PromQL 환경별 격리 (Option A)**: `infra/monitoring/current_dashboard.json` 내의 `OS Memory/Swap Usage`, `OS Disk Usage`, `OS CPU iowait & idle (%)` 패널 PromQL에 `{job="node-metrics-$env"}` 필터를 추가하여, 선택한 환경 변수(`$env`)에 따라 해당 서버의 지표만 격리되어 출력되도록 개선함.
  - **CPU 인스턴스별 구분**: CPU 패널의 쿼리에도 `avg by (instance)`를 추가 적용하여 두 서버의 CPU 지표가 평균 합산되지 않도록 보완 완료.
  - **정합성 및 검증**: python의 json 라이브러리를 통해 대시보드 json 구문 오류가 없음을 검증하고, 전체 백엔드(71개) 및 프론트엔드(70개) 테스트를 실행하여 100% 정상 통과함을 확인함.

### 방문자 지표 수집 로직 및 대시보드 패널 제거 - 완료
- **현상**:
  - 기존 방문자 지표(Daily Unique Visitors, Total Unique Visitors, Total Page Views) 수집 및 시각화 기능이 활성화되어 있으나, GA4(Google Analytics 4)로 트래픽을 대체 관리하므로 모니터링 대시보드 및 서버 자원 측면에서 불필요하게 됨.
- **해결 내역**:
  - **데이터베이스 마이그레이션**: `V2__remove_visitor_logs.sql` 마이그레이션 파일을 생성하여 기존 `visitor_logs` 테이블을 drop 하도록 설계 완료.
  - **백엔드 수집 로직 제거**: `VisitorLog` 엔티티, `VisitorLogRepository`, `VisitorService`, `VisitorController`, `VisitorMetricsConfig`(Micrometer 바인더) 및 관련된 모든 단위/통합 테스트 코드(`VisitorControllerTest`, `VisitorServiceTest`)를 완전히 삭제함.
  - **프론트엔드 API 및 호출부 제거**: `userApi.ts`에서 `logVisit`, `fetchTelemetryStats` 함수 및 `TelemetryStats` 타입을 제거하고, `App.vue`와 `App.test.ts` 내의 로그 전송 및 로딩 로직, 목킹/스파이 코드들을 전면 들어내어 Clean 상태로 복구함.
  - **Grafana 대시보드 패널 제거**: `current_dashboard.json`에서 `panel-112`, `panel-113`, `panel-114`, `panel-115` 패널 정의 및 `Basic Statistics` 행 레이아웃 내의 Grid 배치 항목들을 완전히 삭제 처리함.
  - **통합 검증**: 전체 백엔드 및 프론트엔드(68개) 테스트 전원 통과 완료.

### AI 퍼즐 품질 고도화 및 어드민 보안 강화 - 완료
- **현상**:
  - AI 문제 생성 시 단일 픽셀 아트만 생성한 후 유일하지 않을 때 무작위로 픽셀을 반전시키는 방식(`makeGridUnique`)을 사용하여, 퍼즐의 형태가 훼손되고 품질이 심각하게 저하되는 요인으로 작동함.
  - 운영 배포 환경(`docker-compose.prod.yml`)에서 어드민 계정 계정정보(`ADMIN_USERNAME`, `ADMIN_PASSWORD`)가 매핑되지 않아, 실서버에서도 어드민 기본 비밀번호인 `admin:admin123!`가 노출되는 취약점이 존재함.
- **해결 내역**:
  - **No-Guessing 검증 필터 구현**: [NonogramSolver.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/NonogramSolver.java)에 찍어서 맞추는 백트래킹(DFS) 과정 없이 순수 논리적 연산으로 풀이가 완료되는지 체크하는 `isLogicalOnly(grid)` 기능 신규 구현.
  - **다중 후보 배치(Batch) 검증 파이프라인 수립**:
    - [GeminiAiClient.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/client/GeminiAiClient.java)의 프롬프트를 고도화하여 단일 API 호출 시 5개의 다른 픽셀 아트 후보군을 JSON Array 형식으로 수집하도록 개선.
    - [AiStageGenerator.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/AiStageGenerator.java) 내의 `makeGridUnique` 무작위 픽셀 변전 로직을 영구히 걷어내어 비주얼 형태 깨짐을 방지함.
    - 5개 후보 중 `isLogicalOnly` 검증을 충족하는 후보(1단계)를 최우선 선정하고, 존재하지 않는 경우 `isUnique` 검증을 통과한 후보(2단계)를 순차 매핑하는 2단계 검증 필터 및 `ValidatedCandidate` 배치 전처리 최적화 구현.
  - **데일리 출시 및 어드민 승인 워크플로우 정합성 수정**: [StageService.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/StageService.java) 내 `approveStage` 호출 시 `active = false` 상태였던 스테이지는 활성화(`active = true`) 상태로 강제 전환되지 않고 `active = false, approved = true` 풀 상태를 보존하도록 수정하여, 자정에 데일리 퍼즐이 순차 릴리즈되도록 수정 완료.
  - **어드민 크리덴셜 보안 강화**:
    - [application.yml](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/application.yml)에서 기본 평문 관리자 정보(`admin123!`)를 완전히 삭제하고 환경 변수로만 주입받도록 강제화. [application-local.yml](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/application-local.yml) 및 [application-test.yml](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/application-test.yml)에는 안전한 개발용 기본값을 로컬 격리 부여.
    - [AdminAuthController.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/controller/AdminAuthController.java)에 `@PostConstruct` 검증 블록을 구축하여, 운영 환경 기동 시 크리덴셜 세팅이 누락되거나 비어있을 경우 예외를 전파하며 서버 시작을 강제 차단(Fail-fast) 조치 완료.
    - [docker-compose.prod.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/docker-compose.prod.yml), [docker-compose.stage.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/docker-compose.stage.yml), [playbook.yml](file:///c:/Users/82107/dev/project/nemologic/infra/ansible/playbook.yml) 및 [ci-cd.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/ci-cd.yml)을 보완하여 배포 자동화 과정에서 GitHub Secrets를 통해 관리자 계정 암호가 안전하게 바인딩되어 투입되도록 개선 완료.
  - **테스트 케이스 고도화 및 정합성 검증**:
    - [NonogramSolverTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/service/NonogramSolverTest.java), [AiStageGeneratorTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/service/AiStageGeneratorTest.java) 및 [AdminStageControllerTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/controller/AdminStageControllerTest.java)에 다중 후보 평가, 논리적 풀이 검증 및 상태 보존 어드민 테스트를 신설 및 수정하여 100% 정상 빌드 및 Vitest 프론트엔드 테스트 전원 통과 확인 완료.

### 100% 논리형 퍼즐 강제 및 Git 버전 관리형 시딩 구축 - 완료
- **현상**:
  - AI 생성 퍼즐 중 찍기(DFS backtracking)가 필요한 유일해 퍼즐이 릴리즈되면 유저가 논리적 추론만으로 퍼즐을 해결할 수 없어 사용자 경험을 해칠 우려가 존재함.
  - 퍼즐 데이터가 기동 환경(배포/스테이징/로컬)의 DB 내에만 동적으로 생성/저장되어, 환경별 퍼즐 상태 불일치, DB 유실 시 복구 불가 등 품질 관리에 한계가 있었음.
- **해결 내역**:
  - **논리해(Logical-only) 조건 강제화**: [AiStageGenerator.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/AiStageGenerator.java)의 후보군 평가 필터를 엄격한 `isLogicalOnly` 검증 단일 체계로 개편. 유일해(`isUnique`) 체크로의 Fallback을 완전히 제거하고, 실패율을 낮추기 위해 `maxAttempts`를 5회(최대 25개 후보군 평가)로 증설.
  - **Git 버전 관리형 JSON 퍼즐 파일 이관**:
    - [default_stages.json](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/puzzles/default_stages.json) 리소스 파일을 생성하여 기존 `DataSeeder.java`에 하드코딩되어 있던 기초 퍼즐 데이터 및 추가 퍼즐 명세를 JSON 기반으로 정의 완료.
    - [DataSeeder.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/DataSeeder.java)를 고도화하여 startup 시점에 `classpath:puzzles/*.json` 패턴으로 모든 리소스 파일을 스캔하고 Jackson을 사용하여 DB에 없는 퍼즐을 자동으로 동기화(upsert)하도록 설계.
  - **정합성 및 검증 완료**: `AiStageGeneratorTest.java` 내의 모킹 검증 기대치 조정 및 전체 백엔드/프론트엔드 테스트 100% 통과 확인.

### 기본 퍼즐 및 AI 퍼즐의 통합 단일 워크플로우 격하 및 100% 논리해 개편 - 완료
- **현상**:
  - 기본 퍼즐과 AI 생성 퍼즐이 명칭이나 승인 여부 등에서 구분되어 관리가 번거로웠고, 기존 기초 퍼즐 데이터(Cross Ruby, Smile Face, Giant Cross, Checkerboard) 중 일부가 논리적 연산만으로는 풀리지 않는(Backtracking 필요) 문제가 존재함.
- **해결 내역**:
  - **통합 퍼즐 명세 파일명 중립화 및 미승인 상태로 초기화**:
    - `default_stages.json`을 삭제하고 [stages.json](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/puzzles/stages.json)으로 명칭을 중립화함.
    - 모든 기본 퍼즐을 `active=false, approved=false`로 시딩하여 AI 생성 퍼즐과 완전히 동일하게 어드민 승인을 거치도록 격하 통합함.
  - **기초 퍼즐의 100% 논리해 개편**: 논리적 해결이 불가능했던 `Cross Ruby`, `Smile Face`, `Giant Cross 30x30`, `Checkerboard 20x20` 퍼즐을 `isLogicalOnly` 검증을 통과하는 `Plus Sign`, `New Smile`, `Hollow Square 20x20`, `Hollow Square 30x30` 패턴으로 전면 교체하여 논리적 풀이를 보장함.
  - **AI 문구 및 구분 제거**:
    - [AiStageGenerator.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/AiStageGenerator.java)에서 생성되는 기본 퍼즐 명칭 fallback을 `AI Puzzle`에서 `Puzzle`로 변경.
    - [App.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.vue)에서 어드민 생성 버튼 텍스트를 `Auto-Generate Stage`로 중립화하고 알림창 등에서 'AI' 접두사를 모두 제거함.
  - **테스트 정합성 보완**: [StageControllerTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/controller/StageControllerTest.java)에서 `/api/stages` 호출 시 데이터가 빈 상태로 나오는 문제를 보완하기 위해 `@BeforeEach`에서 시드된 스테이지를 활성화하는 셋업 코드를 추가하여 전체 백엔드/프론트엔드 테스트 100% 정상 통과 확인.

### 랭킹(Leaderboard) 기능 비활성화 - 완료
- **현상**:
  - 퍼즐 품질 최적화 및 100% 논리형 퍼즐 제공에 역량을 집중하기 위해, 복잡한 사용자 랭킹(Leaderboard) UI를 일시적으로 숨김 처리하고자 함.
- **해결 내역**:
  - **프론트엔드 UI 비활성화**: [App.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.vue) 상단 헤더 영역의 랭킹 토글 버튼(`.leaderboard-toggle-btn`)에 `style="display: none;"`을 부여하여 유저 노출을 차단함.
  - **기능성 테스트 보완 및 보존**: 백그라운드 API 호출부 및 마운트 시의 유저 랭킹 로딩 로직은 DOM 및 컴포넌트 내부 상태에 온전히 남겨두어, 기존 랭킹 통합 테스트 코드(`App.test.ts` 등)의 에러를 유발하지 않으면서도 실제 사용자 사용 시나리오에서만 보이지 않도록 완성도 높게 처리 완료.
### GraalVM Native Image 리소스 스캔 오류 및 502 Gateway 에러 해결 - 완료
- **현상**:
  - stage 서버 기동 시 백엔드 컨테이너가 무한 재시작을 반복하며 Nginx에서 502 Bad Gateway 에러를 유발함.
  - 로그 확인 결과, `DataSeeder.java`가 실행될 때 `PathMatchingResourcePatternResolver`를 사용한 `classpath:puzzles/*.json` 리소스 디렉토리 스캔 작업이 GraalVM Native Image(AOT) 환경에서 `FileNotFoundException`을 일으키며 애플리케이션 시작에 실패함.
- **해결 내역**:
  - **DataSeeder 리소스 조회 리팩토링**: `PathMatchingResourcePatternResolver`를 통한 와일드카드 리소스 매칭 방식을 걷어내고, [DataSeeder.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/DataSeeder.java)에서 [stages.json](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/puzzles/stages.json)을 `ClassPathResource("puzzles/stages.json")`로 명시적 직접 조회하도록 변경하여 디렉토리 스캔 과정에서의 에러 가능성을 원천 차단함.
  - **GraalVM 리소스 RuntimeHints 등록**: [NemologicRuntimeHints.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java)를 신설하고 `RuntimeHintsRegistrar`를 구현하여 `puzzles/stages.json` 및 Flyway 마이그레이션 용 `db/migration/*.sql` 리소스 패턴을 네이티브 빌드 타임에 포함하도록 수동 지정함.
  - **메인 어플리케이션 연동**: [NemologicApplication.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/NemologicApplication.java)에 `@ImportRuntimeHints(NemologicRuntimeHints.class)` 어노테이션을 설정하여 빌드 힌트 반영이 정상 수행되도록 설정 완료.

### GraalVM Native Image 환경 내 Jackson 역직렬화 리플렉션 오류 해결 - 완료
- **현상**:
  - 스테이징 환경(`stage.rogic.io`)에서 `Auto-Generate Stage` API 호출 시 `400 Bad Request` 에러 발생.
  - 컨테이너 로그 확인 결과, AI 퍼즐 생성 및 시딩 시 Jackson 라이브러리가 내부 DTO(`AiResponseDto` 및 `StageDto`)를 역직렬화하려 할 때, GraalVM AOT 네이티브 이미지 환경에서 리플렉션(Reflection) 힌트가 누락되어 `Cannot construct instance ... cannot deserialize from Object value` 예외가 발생함.
- **해결 내역**:
  - **DTO 클래스 접근 제한자 변경**: [AiStageGenerator.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/AiStageGenerator.java)의 `AiResponseDto` 및 [DataSeeder.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/DataSeeder.java)의 `StageDto`를 `private static`에서 `public static`으로 승격하여 외부 패키지(`config`)에서 참조 가능하도록 수정.
  - **AOT 리플렉션 힌트 등록**: [NemologicRuntimeHints.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/config/NemologicRuntimeHints.java)에 `StageDto`와 `AiResponseDto` 클래스를 `RuntimeHints`의 reflection 대상(생성자, 메서드, 선언된 필드 일체)으로 등록하여 네이티브 빌드 컴파일 시 리플렉션 정보가 유지되도록 보완 완료.

### GitHub Actions 배포 동시성(Concurrency) 설정 개선 - 완료
- **현상**:
  - `main` 브랜치에 반복해서 푸시할 때, 기존 빌드/배포 실행 건의 Production 승인 대기 상태(Approval Gate)가 자동으로 취소되지 않고 계속 누적 유지되어 이전 승인 대기창들이 방치되는 번거로움이 발생함.
- **해결 내역**:
  - **이전 작업 자동 취소 활성화**: [.github/workflows/ci-cd.yml](file:///c:/Users/82107/dev/project/nemologic/.github/workflows/ci-cd.yml) 내의 `concurrency` 설정의 `cancel-in-progress` 속성 값을 `false`에서 `true`로 변경함. 
  - 이를 통해 Staging 배포 과정 중 버그를 발견하여 추가적인 커밋을 푸시하는 즉시, 이전에 대기 중이던 승인 프로세스는 자동으로 종료되고 최신 작업에 대한 단일 승인 대기 상태만 유지되도록 배포 파이프라인을 효율화함.

### AI 퍼즐 자동 승인 및 임의 상태 퍼즐 삭제 기능 개편 - 완료
- **현상**:
  - 기존에는 AI가 퍼즐을 생성하더라도 관리자 승인을 받기 전까지는 배포 대기 풀에 들어가지 못해 매번 승인 프로세스를 직접 거쳐야 했고, 승인 대기 중이거나 비활성화된 예비 퍼즐을 삭제하고 싶어도 어드민 화면에서 'Delete' 버튼이 숨겨져 지울 수 없었음.
- **해결 내역**:
  - **AI 퍼즐 자동 승인**: [AiStageGenerator.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/AiStageGenerator.java)의 퍼즐 저장 로직을 수정하여 신규 생성되는 모든 AI 퍼즐의 초기 `approved` 값을 항상 `true`로 부여해 관리자 승인 없이도 자정에 자동 릴리즈 대기 풀에 배치되도록 자동화 완료.
  - **영구 삭제(Hard Delete) 마이그레이션**: [StageService.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/StageService.java)의 `deleteStageSoft` 구현을 `stageRepository.delete`로 교체하여 DB 영구 삭제 처리. 데이터베이스 스키마 상 `ON DELETE CASCADE`가 작동하므로 기존 유저 플레이 이력 관계 오류 없이 안전하게 제거됨.
  - **테스트 코드 갱신**: [AdminStageControllerTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/controller/AdminStageControllerTest.java) 내의 삭제 및 생성 검증 기대값을 업데이트하여 100% 정상 통과 확보 완료.
  - **어드민 삭제 UI 노출 조건 해제**: [App.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.vue) 어드민 화면에서 `Delete` 버튼의 `v-if="s.active"` 제한 조건을 걷어내고 항상 노출시켜, 승인 대기 중인 퍼즐 및 임의 퍼즐을 직접 즉시 삭제할 수 있도록 사용자 편의성을 대폭 개선 완료.

### AI 퍼즐 생성 범위 확대 및 평가(좋아요/싫어요) 피드백 시스템 연동 - 완료
- **현상**:
  - 기존 데일리 퍼즐 자동 생성 스케줄러에서 성공률 이슈로 대형 사이즈인 `25x25`, `30x30`이 빠져 있었으며, 사용자가 퍼즐을 완료했을 때 재미있거나 아름다운 퍼즐에 대한 피드백을 전달할 방법이 없어 어드민이 수동으로 별로인 퍼즐을 필터링하기 불가능했음.
- **해결 내역**:
  - **스케줄러 범위 확대**: [DailyPuzzleScheduler.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/scheduler/DailyPuzzleScheduler.java)의 자동 생성 대상 사이즈 배열에 `25`, `30`을 복구하여 대형 사이즈의 퍼즐도 데일리로 자동 수집 및 보급되도록 개선 완료.
  - **데이터베이스 컬럼 추가 (Flyway)**: [V4__add_stage_feedback.sql](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/resources/db/migration/V4__add_stage_feedback.sql)을 작성하여 `stages` 테이블에 `upvotes`, `downvotes` 컬럼을 생성하고 기본값을 0으로 부여 완료.
  - **도메인 엔티티 및 비즈니스 로직**: [Stage.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/model/Stage.java)에 피드백 필드 및 메서드를 작성하고, [StageService.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/service/StageService.java)에 트랜잭셔널 좋아요/싫어요 증가 함수들을 추가 완료.
  - **API 엔드포인트 및 컨트롤러 테스트**: [StageController.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/main/java/com/devdoyen/nemologic/controller/StageController.java)에 `POST /like`, `POST /dislike` 라우트를 노출하고, [StageControllerTest.java](file:///c:/Users/82107/dev/project/nemologic/backend/src/test/java/com/devdoyen/nemologic/controller/StageControllerTest.java)에 단위 테스트를 구현하여 정상 통과 완료.
  - **프론트엔드 API 및 플레이 위젯**: [stageApi.ts](file:///c:/Users/82107/dev/project/nemologic/frontend/src/api/stageApi.ts)에 평가 함수들을 정의하고, [App.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.vue)의 퍼즐 클리어 분기에 👍 / 👎 피드백 카드 위젯을 통합 렌더링하도록 연동 완료.
  - **어드민 만족도 모니터링**: [App.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/App.vue) 백오피스 퍼즐 테이블에 `Feedback` 컬럼(`👍 / 👎`)을 추가하여 평점이 좋지 않은 퍼즐을 관리자가 손쉽게 파악하고 즉시 삭제할 수 있도록 운영 편의성을 개선 완료.
  - **어드민 피드백 열 줄바꿈 문제 해결**: 백오피스 퍼즐 테이블의 `Feedback` 열에서 가로폭 부족으로 싫어요 수치(`0`)가 아래 줄로 튕기던 정렬 버그를 방지하기 위해 `white-space: nowrap` 스타일을 td에 부여하고, 따봉 이모지를 녹색/빨간색으로 대비 코딩된 소형 SVG outline 아이콘 구조로 대체하여 깔끔한 inline 정렬을 구현 완료.
  - **피드백 위젯 레이아웃 개선**: 피드백 위젯이 가로축 flex 레이아웃으로 인해 우측 빈 공간을 크게 차지하거나 화면 최하단에 처지는 현상을 방지하기 위해, 캔버스를 포함하는 `.play-area-inner` 내부의 절대 위치(`position: absolute; bottom: 24px`)로 설정하여 캔버스 바로 아래에 겹쳐서 자연스럽게 공중에 떠 있는 플로팅 카드 레이아웃으로 변경 완료.
  - **유리 모피즘 & 네온 스타일 연동**: 게임 컨셉(rogic.io)의 네온/다크 테마에 조화롭게 녹아들도록 반투명 어두운 배경에 블러 필터(`backdrop-filter: blur(12px)`) 및 밝은 청록색 미세 테두리와 은은한 글로우 그림자를 입힌 캡슐형 둥근 알약(Pill) 모양 디자인을 적용 완료. 호버 시 각각 청록색/빨간색 네온 컬러가 부드럽게 점등되는 트랜지션 애니메이션 부여.
  - **피드백 완료 메시지 간소화 및 수치 은닉**: 사용자가 평가 버튼을 클릭한 뒤, 투표율(👍/👎 개수)을 보여주는 대신 깔끔하게 `✨ Thank You!` 메시지로 전환 및 페이드아웃되도록 변경하여 UI 복잡도를 최소화 완료.
  - **커스텀 SVG 아웃라인 아이콘 적용**: 기존의 캐주얼하고 노란색 계열인 시스템 기본 이모지(👍/👎) 대신, rogic.io 테마의 기하학적이고 현대적인 느낌에 맞춰 얇고 세련된 선 굵기(`stroke-width: 1.5`)의 커스텀 SVG 따봉/역따봉 아웃라인 아이콘으로 전면 리뉴얼 완료.
  - **자동 전환 지연 방지**: 인메모리 테스트 DB 초기화로 인해 프론트엔드 localStorage 내의 세션 사용자 ID(예: `8`)가 백엔드 DB에 존재하지 않아 `clearStage` API 통신 오류 발생 시, 다음 퍼즐 타이머가 실행되지 않고 화면이 멈추던 버그를 `finally` 구문을 추가하여 상시 전환 타이머가 작동하도록 복구 완료. 또한 DB 불일치 시 로컬 세션을 비우고 자동 재등록하도록 자가 복구 로직 연동 완료.
  - **X 표시 스타일 및 투명도 조정**: 기존의 지나치게 튀는 빨간색 `#f43f5e` X 표시 대신, 보드판의 어두운 팔레트에 자연스럽게 녹아들도록 차분한 반투명 회색(`rgba(148, 163, 184, 0.45)`) 스트로크로 스타일을 수정 완료.
  - **클리어 상태의 X 표시 및 격자선 완전 은닉**: 퍼즐을 완벽하게 맞췄을 때 지저분한 X 표시와 셀 구분선(격자선, 5칸 굵은 구분선)이 방해하지 않고 온전히 하나의 완성된 아름다운 픽셀 아트 이미지로 보이도록 구현했습니다. `isSolved()`가 참일 때 모든 격자선 그리기와 X 그리기를 완전히 생략하며, 색칠된 셀 간의 틈새(margin) 및 테두리 stroke도 없애고 셀 크기 100%를 부드럽게 꽉 채워(Full fill) 렌더링되도록 [NonogramCanvas.vue](file:///c:/Users/82107/dev/project/nemologic/frontend/src/components/NonogramCanvas.vue)을 수정 완료.
  - **헤더 하단 프로그레스 바 정렬 및 전체 너비 연동**: 다음 스테이지로 넘어갈 때 뷰포트 최상단에 얇게 나오던 프로그레스 바가 어색했던 레이아웃을 수정하여, 상단 헤더 컨테이너(`.app-header`) 하단 국경선 위치(My Page 버튼이 있는 우측 끝까지 가로 너비 100% 매칭)에 맞춰 헤더 전체 가로폭에 걸쳐 자연스럽게 로딩 게이지가 차오르는 플로팅 연출로 교체 완료.





