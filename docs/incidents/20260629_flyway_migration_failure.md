# [Incident Report] 2026-06-29 - Flyway DB Migration Failure (FK Constraint & Checksum Mismatch)

## 1. 개요 (Summary)
*   **장애 발생 일시:** 2026-06-29 11:23 KST (02:23 UTC)
*   **장애 복구 일시:** 2026-06-29 12:20 KST (03:20 UTC)
*   **장애 지속 시간:** 57분 (프로덕션 복구 22분, 스테이징 최종 복구 57분)
*   **영향 범위:** 프로덕션(Production) 및 스테이지(Stage) 환경 백엔드 서비스 배포 차단
*   **장애 원인:** 
    1. **1차 원인 (FK Constraint Violation):** 데이터베이스 스키마 마이그레이션 중 외래키 제약조건 위반 (참조 데이터가 존재하는 상태에서 부모 레코드 삭제 시도)
    2. **2차 원인 (Checksum Mismatch):** 프로덕션 복구를 위해 마이그레이션 스크립트를 수정한 후, 이미 기존 마이그레이션이 반영되어 있던 스테이징 환경에서 파일 체크섬 불일치 발생

---

## 2. 장애 타임라인 (Timeline)
*   **11:23 KST (02:23 UTC):** GitHub Actions를 통한 프로덕션 배포 파이프라인 가동 중 백엔드 블루/그린 컨테이너 교체 단계에서 실패 감지 (장애 시작).
*   **11:34 KST (02:34 UTC):** `V3__clean_up_old_stages.sql` 내 `DELETE FROM stages` 구문이 `histories` 테이블의 외래키 제약조건(`fkk7t9a00lti7i5qc0owqx62qin`)을 위반하는 에러 분석 완료.
*   **11:37 KST (02:37 UTC):** 마이그레이션 스크립트에 연계 데이터(`histories`) 선삭제 쿼리 추가 조치 및 로컬 검증 완료 후 Git 커밋 반영.
*   **11:45 KST (02:45 UTC):** 수정 코드가 반영된 프로덕션 서버 배포 성공 (프로덕션 복구 완료).
*   **11:58 KST (02:58 UTC):** 수정된 코드로 스테이징 배포 파이프라인 가동 중 `FlywayValidateException: Migration checksum mismatch for migration version 3` 에러와 함께 스테이징 컨테이너 기동 실패 감지.
*   **12:05 KST (03:05 UTC):** 분석 완료. 스테이징 디비에는 선삭제 쿼리가 없는 구버전 V3가 이미 기록되어 체크섬(`-1872626081`) 불일치가 발생한 것으로 확인.
*   **12:08 KST (03:08 UTC):** 스테이징 DB 컨테이너(`nemologic-db`)에 수동으로 접속하여 `flyway_schema_history` 내 V3 체크섬을 로컬 수정본의 체크섬(`1640573210`)으로 업데이트 실행.
*   **12:20 KST (03:20 UTC):** 스테이징 배포 정상 재개 및 서비스 정상화 확인 (장애 종료).

---

## 3. 원인 분석 (Root Cause Analysis)
*   **1차 오류 (프로덕션):**
    기존 비논리 퍼즐을 초기화하고 신규 테마 데이터로 갱신하기 위해 `V3__clean_up_old_stages.sql`에서 특정 기본 스테이지들의 삭제 작업을 시도했습니다. 하지만 프로덕션에서는 이미 유저들의 플레이 이력(`histories`)이 외래키 제약조건으로 묶여 있었기 때문에 삭제 쿼리가 차단되었습니다.
*   **2차 오류 (스테이징):**
    프로덕션 오류 복구를 위해 이미 실행된 적 있는 `V3` 마이그레이션 파일 내용을 수정 및 변경하여 배포를 진행한 결과, 스테이징 디비가 기존에 기록하고 있던 V3 해시(체크섬)와 일치하지 않아 Flyway 기동 검증(Validate) 단계에서 불일치 차단이 발생했습니다.

---

## 4. 해결 방안 (Resolution)
1.  **외래키 위반 해결:** 부모 레코드 삭제 전에 자식 레코드 중 관련 있는 참조 데이터를 안전하게 먼저 삭제하도록 마이그레이션 쿼리를 개선하여 반영했습니다.
    ```sql
    -- histories 테이블의 참조 데이터 우선 삭제
    DELETE FROM histories WHERE stage_id IN (
        SELECT id FROM stages 
        WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30')
    );
    -- stages 물리 삭제 수행
    DELETE FROM stages WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30');
    ```
2.  **체크섬 불일치 해결:** 스테이징 DB 컨테이너 내부로 진입하여 테이블 이력 체크섬을 강제 갱신하여 빌드 검증을 활성화했습니다.
    ```bash
    docker exec -i nemologic-db psql -U postgres -d nemologic -c "UPDATE flyway_schema_history SET checksum = 1640573210 WHERE version = '3';"
    ```

---

## 5. 재발 방지 대책 (Preventative Actions)
1.  **DML 마이그레이션 설계 주의:** 향후 Flyway 마이그레이션 중 테이블 레코드를 삭제(`DELETE`)하거나 수정(`UPDATE`로 ID 변경 등)할 때는 관련된 외래키 참조 관계를 사전 검토하는 프로세스를 확립합니다.
2.  **스테이지 비활성화(Soft Delete) 지향:** 실제 상용 데이터베이스 설계 시, 데이터 정합성과 유저 히스토리 유지를 위해 물리 삭제(`DELETE`) 대신 `active = false` 형태의 논리 삭제(Soft Delete) 처리를 지향하도록 변경합니다.
3.  **마이그레이션 불변성 준수:** 이미 배포되어 타 환경에 성공 반영된 마이그레이션 스크립트는 임의 수정하지 않고, 항상 버전을 올린 신규 마이그레이션 파일(예: `V5__...`)을 생성하여 추가 실행하는 패러다임을 의무화합니다.

