# [Incident Report] 2026-06-29 - Flyway DB Migration Failure (FK Constraint Violation)

## 1. 개요 (Summary)
*   **장애 발생 일시:** 2026-06-29 11:23 KST (02:23 UTC)
*   **장애 복구 일시:** 2026-06-29 11:45 KST (02:45 UTC)
*   **장애 지속 시간:** 22분
*   **영향 범위:** 프로덕션(Production) 및 스테이지(Stage) 환경 백엔드 서비스 전면 중단 (배포 롤백으로 인해 기존 구버전 인스턴스가 대기하는 상태에서 신규 배포 실패)
*   **장애 원인:** 데이터베이스 스키마 마이그레이션 중 외래키 제약조건 위반 (참조 데이터가 존재하는 상태에서 부모 레코드 삭제 시도)

---

## 2. 장애 타임라인 (Timeline)
*   **11:23 KST (02:23 UTC):** GitHub Actions를 통한 프로덕션 배포 파이프라인(Ansible Playbook) 가동 중 백엔드 블루/그린 컨테이너 교체 단계에서 실패 감지.
*   **11:30 KST (02:30 UTC):** 개발자 모니터링 중 서버 미작동 및 배포 오류 로그 접수.
*   **11:34 KST (02:34 UTC):** 에러 로그 분석 완료. `V3__clean_up_old_stages.sql` 내 `DELETE FROM stages` 구문이 `histories` 테이블의 외래키 제약조건(`fkk7t9a00lti7i5qc0owqx62qin`)을 위반하는 것을 확인.
*   **11:37 KST (02:37 UTC):** 마이그레이션 스크립트에 연계 데이터(`histories`) 선삭제 쿼리 추가 조치 및 로컬 검증 완료 후 Git 커밋 반영.
*   **11:38 KST (02:38 UTC):** 수정된 마이그레이션 코드를 원격 저장소(`main`)로 푸시.
*   **11:45 KST (02:45 UTC):** 마이그레이션이 정상 통과하며 신규 백엔드 컨테이너가 정상적으로 부팅 및 배포 완결됨 (장애 종료).

---

## 3. 원인 분석 (Root Cause Analysis)
*   **현상 상세:**
    기존 비논리 퍼즐을 초기화하고 신규 테마 데이터로 갱신하기 위해 `V3__clean_up_old_stages.sql`에서 아래 삭제 작업을 시도했습니다:
    ```sql
    DELETE FROM stages WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30');
    ```
    하지만 실사용 환경(Stage/Prod)에서는 이미 유저들이 해당 스테이지를 플레이하여 이력이 `histories` 테이블에 쌓여 있었고, `histories.stage_id`가 `stages.id`를 외래키로 참조하고 있는 구조였습니다.
*   **직접적 원인:**
    참조하는 자식 테이블(`histories`) 레코드가 남아 있는 상태에서 부모 테이블(`stages`)을 물리 삭제하려 함으로써 `PgException` 및 Flyway 마이그레이션 실패가 일어났습니다.

---

## 4. 해결 방안 (Resolution)
부모 레코드 삭제 전에 자식 레코드 중 관련 있는 참조 데이터를 안전하게 먼저 삭제하도록 마이그레이션 쿼리를 개선하여 반영했습니다.
```sql
-- 1. 외래키 위반 방지를 위해 histories 테이블의 참조 데이터 우선 삭제
DELETE FROM histories WHERE stage_id IN (
    SELECT id FROM stages 
    WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30')
);

-- 2. 부모 테이블(stages)의 스테이지 물리 삭제 수행
DELETE FROM stages WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30');
```

---

## 5. 재발 방지 대책 (Preventative Actions)
1.  **DML 마이그레이션 설계 주의:** 향후 Flyway 마이그레이션 중 테이블 레코드를 삭제(`DELETE`)하거나 수정(`UPDATE`로 ID 변경 등)할 때는 관련된 외래키 참조 관계를 사전 검토하는 프로세스를 확립합니다.
2.  **스테이지 비활성화(Soft Delete) 지향:** 실제 상용 데이터베이스 설계 시, 데이터 정합성과 유저 히스토리 유지를 위해 물리 삭제(`DELETE`) 대신 `active = false` 형태의 논리 삭제(Soft Delete) 처리를 지향하도록 변경합니다.
3.  **스테이징 검증 환경 강화:** 스테이징 DB에는 가급적 프로덕션 DB와 유사한 더미 히스토리 데이터를 상시 누적시켜 마이그레이션 배포 시 제약조건 예외가 미리 감지되도록 구성합니다.
