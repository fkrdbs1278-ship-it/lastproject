-- =====================================================================
-- V008__add_gender_to_hair_style.sql
-- HAIR_STYLE 성별 구분 컬럼 추가
--
-- GENDER 값
--   M   : 남성 스타일
--   F   : 여성 스타일
--   ALL : 성별 공통 / 기타
--
-- 기존 CATEGORY 기준 데이터 보정
--   MEN                         -> M
--   SHORT / MEDIUM / LONG / WOMEN -> F
--   ETC / NULL / 기타          -> ALL
-- =====================================================================

-- 1. 컬럼 추가
ALTER TABLE HAIR_STYLE
    ADD GENDER VARCHAR2(10);

-- 2. 기존 데이터 보정
UPDATE HAIR_STYLE
SET GENDER =
    CASE
        WHEN CATEGORY = 'MEN' THEN 'M'
        WHEN CATEGORY IN ('SHORT', 'MEDIUM', 'LONG', 'WOMEN') THEN 'F'
        ELSE 'ALL'
    END
WHERE GENDER IS NULL;

-- 3. 기본값 및 NOT NULL 적용
ALTER TABLE HAIR_STYLE
    MODIFY GENDER DEFAULT 'ALL' NOT NULL;

-- 4. 허용값 CHECK 제약조건 추가
ALTER TABLE HAIR_STYLE
    ADD CONSTRAINT HAIR_STYLE_GENDER_CK
        CHECK (GENDER IN ('M', 'F', 'ALL'));

-- 5. 성별 + 카테고리 + 활성 여부 조회용 인덱스
CREATE INDEX IDX_HAIR_STYLE_GENDER_CATEGORY
    ON HAIR_STYLE(GENDER, CATEGORY, ACTIVE_YN);

COMMIT;
