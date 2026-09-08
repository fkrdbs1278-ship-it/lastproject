-- =====================================================================
-- V005__add_material_usage_columns.sql
-- 목적:
--   최신 Material 엔티티와 기존 MATERIAL 테이블 간 스키마 차이를 보정합니다.
--
-- 추가 컬럼:
--   CONTENT_QUANTITY         NUMBER(12, 2)
--   USAGE_UNIT_CODE          VARCHAR2(20)
--   OPEN_REMAINING_QUANTITY  NUMBER(12, 2)
--
-- 특징:
--   - 기존 MATERIAL 데이터는 삭제하지 않습니다.
--   - 세 컬럼은 현재 엔티티 기준 nullable이므로 기존 행의 값은 NULL로 유지됩니다.
--   - 이미 컬럼이 존재하는 DB에서도 다시 실행할 수 있도록
--     USER_TAB_COLUMNS를 확인한 뒤 없는 컬럼만 추가합니다.
--   - Oracle 기준입니다.
-- =====================================================================

SET DEFINE OFF;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'MATERIAL'
       AND COLUMN_NAME = 'CONTENT_QUANTITY';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE MATERIAL ADD (CONTENT_QUANTITY NUMBER(12, 2))';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'MATERIAL'
       AND COLUMN_NAME = 'USAGE_UNIT_CODE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE MATERIAL ADD (USAGE_UNIT_CODE VARCHAR2(20))';
    END IF;
END;
/

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'MATERIAL'
       AND COLUMN_NAME = 'OPEN_REMAINING_QUANTITY';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE MATERIAL ADD (OPEN_REMAINING_QUANTITY NUMBER(12, 2))';
    END IF;
END;
/

-- =====================================================================
-- 적용 확인
-- 세 행이 조회되면 정상입니다.
-- =====================================================================

SELECT
    COLUMN_NAME,
    DATA_TYPE,
    DATA_LENGTH,
    DATA_PRECISION,
    DATA_SCALE,
    NULLABLE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'MATERIAL'
  AND COLUMN_NAME IN (
      'CONTENT_QUANTITY',
      'USAGE_UNIT_CODE',
      'OPEN_REMAINING_QUANTITY'
  )
ORDER BY COLUMN_ID;

-- =====================================================================
-- 참고: 롤백이 꼭 필요한 경우에만 아래 SQL을 별도로 실행하세요.
-- 기존 데이터가 존재할 수 있으므로 자동 롤백 구문에는 포함하지 않습니다.
--
-- ALTER TABLE MATERIAL DROP COLUMN CONTENT_QUANTITY;
-- ALTER TABLE MATERIAL DROP COLUMN USAGE_UNIT_CODE;
-- ALTER TABLE MATERIAL DROP COLUMN OPEN_REMAINING_QUANTITY;
-- =====================================================================
