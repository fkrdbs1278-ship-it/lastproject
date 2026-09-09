-- =====================================================================
-- V002__review_reservation_constraints.sql
-- 목적:
--   1) REVIEW.RESERVATION_NO를 NOT NULL로 변경
--   2) 예약 1건당 리뷰 1건만 작성 가능하도록 UNIQUE 제약 추가
--
-- 대상:
--   기존 salon_crm_final_schema.sql을 이미 적용한 팀원
--
-- 특징:
--   - 테이블 DROP / 재생성 없음
--   - 기존 데이터 보존
--   - 이미 적용된 경우 다시 실행해도 최대한 안전하게 건너뜀
--   - 잘못된 기존 데이터(NULL / 중복)가 있으면 삭제하지 않고 명확히 중단
-- =====================================================================


-- ---------------------------------------------------------------------
-- 0. REVIEW 테이블 존재 여부 확인
-- ---------------------------------------------------------------------
DECLARE
    V_TABLE_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_TABLE_COUNT
      FROM USER_TABLES
     WHERE TABLE_NAME = 'REVIEW';

    IF V_TABLE_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20000,
            'REVIEW 테이블이 없습니다. 먼저 기본 스키마(V001)를 적용하세요.'
        );
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- 1. 사전 검증: RESERVATION_NO가 NULL인 기존 리뷰가 있는지 확인
--
-- NOT NULL 적용 전에 반드시 0건이어야 합니다.
-- 임의 삭제/수정은 하지 않습니다.
-- ---------------------------------------------------------------------
DECLARE
    V_NULL_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_NULL_COUNT
      FROM REVIEW
     WHERE RESERVATION_NO IS NULL;

    IF V_NULL_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'REVIEW.RESERVATION_NO가 NULL인 데이터가 '
            || V_NULL_COUNT
            || '건 있습니다. 해당 리뷰에 올바른 예약번호를 연결한 후 migration을 다시 실행하세요.'
        );
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- 2. 사전 검증: 하나의 예약번호에 리뷰가 여러 건 존재하는지 확인
--
-- UNIQUE 적용 전에 반드시 중복 예약번호가 없어야 합니다.
-- ---------------------------------------------------------------------
DECLARE
    V_DUPLICATE_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_DUPLICATE_COUNT
      FROM (
            SELECT RESERVATION_NO
              FROM REVIEW
             WHERE RESERVATION_NO IS NOT NULL
             GROUP BY RESERVATION_NO
            HAVING COUNT(*) > 1
      );

    IF V_DUPLICATE_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            '동일 RESERVATION_NO를 사용하는 중복 리뷰 그룹이 '
            || V_DUPLICATE_COUNT
            || '개 있습니다. 중복 데이터를 먼저 정리한 후 migration을 다시 실행하세요.'
        );
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- 3. REVIEW.RESERVATION_NO -> NOT NULL
--
-- 이미 NOT NULL이면 아무 작업도 하지 않습니다.
-- ---------------------------------------------------------------------
DECLARE
    V_NULLABLE VARCHAR2(1);
BEGIN
    SELECT NULLABLE
      INTO V_NULLABLE
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'REVIEW'
       AND COLUMN_NAME = 'RESERVATION_NO';

    IF V_NULLABLE = 'Y' THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE REVIEW MODIFY RESERVATION_NO NOT NULL';
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- 4. 예약 1건당 리뷰 1건 UNIQUE 제약 추가
--
-- 1번 담당자가 이미 REVIEW_RESERVATION_UK 이름으로 적용했다면 건너뜁니다.
-- ---------------------------------------------------------------------
DECLARE
    V_CONSTRAINT_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_CONSTRAINT_COUNT
      FROM USER_CONSTRAINTS
     WHERE TABLE_NAME = 'REVIEW'
       AND CONSTRAINT_NAME = 'REVIEW_RESERVATION_UK'
       AND CONSTRAINT_TYPE = 'U';

    IF V_CONSTRAINT_COUNT = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE REVIEW '
            || 'ADD CONSTRAINT REVIEW_RESERVATION_UK '
            || 'UNIQUE (RESERVATION_NO)';
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- 5. 적용 결과 확인
-- ---------------------------------------------------------------------
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    NULLABLE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'REVIEW'
  AND COLUMN_NAME = 'RESERVATION_NO';


SELECT
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    STATUS
FROM USER_CONSTRAINTS
WHERE TABLE_NAME = 'REVIEW'
  AND CONSTRAINT_NAME = 'REVIEW_RESERVATION_UK';


-- 기대 결과
--   REVIEW.RESERVATION_NO NULLABLE = N
--   REVIEW_RESERVATION_UK CONSTRAINT_TYPE = U
--   REVIEW_RESERVATION_UK STATUS = ENABLED
-- =====================================================================
