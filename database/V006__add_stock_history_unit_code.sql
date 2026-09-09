-- =====================================================================
-- V006__add_stock_history_unit_code.sql
-- 목적:
--   최신 StockHistory 엔티티와 기존 STOCK_HISTORY 테이블 간
--   스키마 차이를 보정합니다.
--
-- 추가 컬럼:
--   UNIT_CODE VARCHAR2(20)
--
-- 특징:
--   - 기존 STOCK_HISTORY 데이터는 삭제하지 않습니다.
--   - UNIT_CODE는 현재 엔티티 기준 nullable이므로 기존 행은 NULL 유지
--   - 이미 컬럼이 존재하는 DB에서도 다시 실행할 수 있도록
--     USER_TAB_COLUMNS를 확인한 뒤 없는 경우에만 추가
--   - Oracle 기준
-- =====================================================================

SET DEFINE OFF;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_count
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'STOCK_HISTORY'
       AND COLUMN_NAME = 'UNIT_CODE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE STOCK_HISTORY ADD (UNIT_CODE VARCHAR2(20))';
    END IF;
END;
/

-- =====================================================================
-- 적용 확인
-- 1행이 조회되면 정상입니다.
-- =====================================================================

SELECT
    COLUMN_NAME,
    DATA_TYPE,
    DATA_LENGTH,
    DATA_PRECISION,
    DATA_SCALE,
    NULLABLE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'STOCK_HISTORY'
  AND COLUMN_NAME = 'UNIT_CODE';

-- =====================================================================
-- 참고: 롤백이 반드시 필요한 경우에만 별도로 실행
--
-- ALTER TABLE STOCK_HISTORY DROP COLUMN UNIT_CODE;
-- =====================================================================
