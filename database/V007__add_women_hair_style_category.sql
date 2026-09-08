-- =====================================================================
-- V007__add_women_hair_style_category.sql
-- 목적:
--   HAIR_STYLE.CATEGORY에 WOMEN 값을 허용합니다.
--
-- 기존 허용값:
--   SHORT / MEDIUM / LONG / MEN / ETC
--
-- 변경 후:
--   SHORT / MEDIUM / LONG / MEN / WOMEN / ETC
--
-- 데이터 삭제 없음 / Oracle 기준
-- =====================================================================

SET DEFINE OFF;

DECLARE
    V_TABLE_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_TABLE_COUNT
      FROM USER_TABLES
     WHERE TABLE_NAME = 'HAIR_STYLE';

    IF V_TABLE_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20070,
            'HAIR_STYLE 테이블이 없습니다. 먼저 기본 스키마를 적용하세요.'
        );
    END IF;
END;
/

DECLARE
    V_CONSTRAINT_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO V_CONSTRAINT_COUNT
      FROM USER_CONSTRAINTS
     WHERE TABLE_NAME = 'HAIR_STYLE'
       AND CONSTRAINT_NAME = 'HAIR_STYLE_CATEGORY_CK';

    IF V_CONSTRAINT_COUNT > 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE HAIR_STYLE DROP CONSTRAINT HAIR_STYLE_CATEGORY_CK';
    END IF;
END;
/

ALTER TABLE HAIR_STYLE
ADD CONSTRAINT HAIR_STYLE_CATEGORY_CK
CHECK (
    CATEGORY IN (
        'SHORT',
        'MEDIUM',
        'LONG',
        'MEN',
        'WOMEN',
        'ETC'
    )
    OR CATEGORY IS NULL
);

SELECT
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    STATUS,
    SEARCH_CONDITION
FROM USER_CONSTRAINTS
WHERE TABLE_NAME = 'HAIR_STYLE'
  AND CONSTRAINT_NAME = 'HAIR_STYLE_CATEGORY_CK';

SELECT
    CATEGORY,
    COUNT(*) AS CNT
FROM HAIR_STYLE
GROUP BY CATEGORY
ORDER BY CATEGORY;
