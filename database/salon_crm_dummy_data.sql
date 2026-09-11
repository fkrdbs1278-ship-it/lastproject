-- =====================================================================
-- salon_crm_dummy_data.sql
-- Oracle Autonomous Database 26ai / 신규 빈 스키마용 초기 데이터
-- 실행 순서: salon_crm_final_schema.sql -> 이 파일
-- =====================================================================

-- 고객 등급
INSERT INTO CUSTOMER_GRADE (GRADE_CODE, GRADE_NAME, GRADE_DESCRIPTION, GRADE_PRIORITY)
VALUES ('NORMAL', '일반', '신규 고객 또는 방문 2회 이하', 1);
INSERT INTO CUSTOMER_GRADE (GRADE_CODE, GRADE_NAME, GRADE_DESCRIPTION, GRADE_PRIORITY)
VALUES ('REGULAR', '단골', '방문 3회 이상', 2);
INSERT INTO CUSTOMER_GRADE (GRADE_CODE, GRADE_NAME, GRADE_DESCRIPTION, GRADE_PRIORITY)
VALUES ('VIP', 'VIP', '방문 10회 이상 또는 누적 결제 100만원 이상', 3);

-- 기본 영업시간
INSERT ALL
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (1, 'Y', '10:00', '20:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (2, 'Y', '10:00', '20:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (3, 'Y', '10:00', '20:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (4, 'Y', '10:00', '20:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (5, 'Y', '10:00', '20:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (6, 'Y', '10:00', '18:00')
  INTO BUSINESS_HOUR (DAY_OF_WEEK, IS_OPEN, OPEN_TIME, CLOSE_TIME) VALUES (7, 'N', NULL, NULL)
SELECT 1 FROM DUAL;

-- siteadmin 모듈 기본 화면 설정
INSERT INTO SITE_SETTING
  (HERO_TITLE, HERO_DESCRIPTION, HERO_IMAGE_URL, SERVICE_VISIBLE,
   SERVICE_TITLE, STYLE_VISIBLE, STYLE_TITLE, STYLE_DESCRIPTION)
VALUES
  ('당신만을 위한 1인 미용실',
   '한 분 한 분에게 집중하는 예약제 헤어 살롱입니다.',
   '/images/main/hero.jpg', 'Y', '서비스 안내', 'Y',
   '헤어스타일 둘러보기', '원하는 스타일을 살펴보고 바로 예약해 보세요.');

-- SERVICE_MATERIAL 연결용 기본 자재
INSERT ALL
  INTO MATERIAL (MATERIAL_NAME, CATEGORY_CODE, UNIT_CODE, CONTENT_QUANTITY, USAGE_UNIT_CODE, OPEN_REMAINING_QUANTITY, CURRENT_STOCK, SAFETY_STOCK, UNIT_PRICE, SUPPLIER_NAME)
    VALUES ('기본 샴푸', 'SHAMPOO', 'EA', 1000, 'ML', 1000, 10, 3, 18000, '살롱 공급사')
  INTO MATERIAL (MATERIAL_NAME, CATEGORY_CODE, UNIT_CODE, CONTENT_QUANTITY, USAGE_UNIT_CODE, OPEN_REMAINING_QUANTITY, CURRENT_STOCK, SAFETY_STOCK, UNIT_PRICE, SUPPLIER_NAME)
    VALUES ('펌제', 'PERM', 'EA', 500, 'ML', 500, 8, 2, 25000, '살롱 공급사')
  INTO MATERIAL (MATERIAL_NAME, CATEGORY_CODE, UNIT_CODE, CONTENT_QUANTITY, USAGE_UNIT_CODE, OPEN_REMAINING_QUANTITY, CURRENT_STOCK, SAFETY_STOCK, UNIT_PRICE, SUPPLIER_NAME)
    VALUES ('컬러제', 'COLOR', 'EA', 200, 'G', 200, 12, 3, 15000, '살롱 공급사')
  INTO MATERIAL (MATERIAL_NAME, CATEGORY_CODE, UNIT_CODE, CONTENT_QUANTITY, USAGE_UNIT_CODE, OPEN_REMAINING_QUANTITY, CURRENT_STOCK, SAFETY_STOCK, UNIT_PRICE, SUPPLIER_NAME)
    VALUES ('클리닉 트리트먼트', 'CLINIC', 'EA', 500, 'ML', 500, 7, 2, 30000, '살롱 공급사')
SELECT 1 FROM DUAL;

-- =====================================================================
-- V012__insert_service_menu_common_seed.sql
--
-- 모든 팀원이 동일한 예약 상세 시술 메뉴를 사용하도록 하는 공통 seed입니다.
-- 새로 만든 빈 최종 DB에 1회 실행하는 것을 전제로 합니다.
--
-- 예약 화면 1단계 카테고리:
-- CUT / PERM / COLOR / CLINIC / ETC
--
-- 예약 화면 2단계:
-- 아래 SERVICE_MENU 실제 행을 사용합니다.
-- =====================================================================

-- CUT
INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CUT', '커트', '기본 커트 시술', 20000, 30, 'Y', 110);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CUT', '남성 커트', '남성 기본 커트 시술', 20000, 30, 'Y', 120);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CUT', '여성 커트', '여성 기본 커트 시술', 25000, 40, 'Y', 130);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CUT', '앞머리 커트', '앞머리 정리 커트', 10000, 15, 'Y', 140);

-- PERM
INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('PERM', '펌', '기본 펌 시술', 80000, 120, 'Y', 210);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('PERM', '일반 펌', '기본 디자인의 일반 펌', 80000, 120, 'Y', 220);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('PERM', '디자인 펌', '스타일에 맞춘 디자인 펌', 100000, 150, 'Y', 230);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('PERM', '다운 펌', '옆머리와 뜨는 부분을 정리하는 다운 펌', 40000, 60, 'Y', 240);

-- COLOR
INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('COLOR', '컬러', '기본 컬러 시술', 70000, 120, 'Y', 310);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('COLOR', '전체 컬러', '전체 모발 컬러 시술', 70000, 120, 'Y', 320);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('COLOR', '뿌리 컬러', '자라난 뿌리 부분 컬러 시술', 50000, 90, 'Y', 330);

-- CLINIC
INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CLINIC', '클리닉', '기본 모발 클리닉', 50000, 60, 'Y', 410);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CLINIC', '기본 클리닉', '기본 모발 케어 클리닉', 50000, 60, 'Y', 420);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('CLINIC', '집중 클리닉', '손상 모발 집중 케어 클리닉', 80000, 90, 'Y', 430);

-- ETC
INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('ETC', '기타', '기타 상담형 시술', 30000, 30, 'Y', 510);

INSERT INTO SERVICE_MENU
    (CATEGORY, NAME, DESCRIPTION, PRICE, DURATION_MIN, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('ETC', '샴푸·드라이', '샴푸 후 기본 드라이 시술', 20000, 30, 'Y', 520);

COMMIT;


-- HAIR_STYLE final seed data
-- Source: HAIR_STYLE_fixed_1122x1402(1).zip
-- 57 image files exist in ZIP and all 57 are inserted.
-- ETC color files use -f = F (female), -m = M (male).
-- Run this on an empty HAIR_STYLE table (or after confirming duplicates do not exist).

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('애쉬 브라운', 'ETC', 'F', '차분한 회갈색 계열의 애쉬 브라운 컬러', '/images/hairstyle/ETC/ash-brown-color-f.png', 'Y', 1);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('애쉬 브라운', 'ETC', 'M', '차분한 회갈색 계열의 애쉬 브라운 컬러', '/images/hairstyle/ETC/ash-brown-color-m.png', 'Y', 2);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('브라운', 'ETC', 'F', '자연스럽고 기본적인 브라운 컬러', '/images/hairstyle/ETC/brown-color-f.png', 'Y', 3);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('브라운', 'ETC', 'M', '자연스럽고 기본적인 브라운 컬러', '/images/hairstyle/ETC/brown-color-m.png', 'Y', 4);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('초콜릿 브라운', 'ETC', 'F', '진하고 부드러운 느낌의 초콜릿 브라운 컬러', '/images/hairstyle/ETC/chocolate-brown-color-f.png', 'Y', 5);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('초콜릿 브라운', 'ETC', 'M', '진하고 부드러운 느낌의 초콜릿 브라운 컬러', '/images/hairstyle/ETC/chocolate-brown-color-m.png', 'Y', 6);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('핑크 브라운', 'ETC', 'F', '은은한 핑크빛이 들어간 브라운 컬러', '/images/hairstyle/ETC/pink-brown-color-f.png', 'Y', 7);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('핑크 브라운', 'ETC', 'M', '은은한 핑크빛이 들어간 브라운 컬러', '/images/hairstyle/ETC/pink-brown-color-m.png', 'Y', 8);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('레드 브라운', 'ETC', 'F', '붉은빛이 자연스럽게 섞인 브라운 컬러', '/images/hairstyle/ETC/red-brown-color-f.png', 'Y', 9);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('레드 브라운', 'ETC', 'M', '붉은빛이 자연스럽게 섞인 브라운 컬러', '/images/hairstyle/ETC/red-brown-color-m.png', 'Y', 10);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('빅시컷', 'SHORT', 'F', '보브와 픽시컷의 특징을 결합한 짧은 스타일', '/images/hairstyle/SHORT/bixie-cut.png', 'Y', 11);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('블런트 보브', 'SHORT', 'F', '끝선을 깔끔하게 정리한 단발 스타일', '/images/hairstyle/SHORT/blunt-bob.png', 'Y', 12);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('보브컷', 'SHORT', 'F', '깔끔하고 자연스러운 단발 스타일', '/images/hairstyle/SHORT/bob.png', 'Y', 13);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('크롭컷', 'SHORT', 'F', '짧고 가볍게 정리한 크롭 스타일', '/images/hairstyle/SHORT/crop.png', 'Y', 14);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('허쉬 보브', 'SHORT', 'F', '가벼운 층을 살린 허쉬 단발 스타일', '/images/hairstyle/SHORT/hush-bob.png', 'Y', 15);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('픽시컷', 'SHORT', 'F', '짧고 가벼운 느낌의 픽시 스타일', '/images/hairstyle/SHORT/pixie-cut.png', 'Y', 16);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('숏 레이어드', 'SHORT', 'F', '층을 활용해 가벼운 느낌을 살린 숏 스타일', '/images/hairstyle/SHORT/short-layered.png', 'Y', 17);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('빌드펌', 'MEDIUM', 'F', '자연스러운 볼륨과 컬을 살린 미디엄 펌', '/images/hairstyle/MEDIUM/build-perm.png', 'Y', 18);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('허쉬컷', 'MEDIUM', 'F', '층을 가볍게 살린 미디엄 허쉬 스타일', '/images/hairstyle/MEDIUM/hush-cut.png', 'Y', 19);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('미디엄 C컬', 'MEDIUM', 'F', '모발 끝에 C컬을 살린 미디엄 스타일', '/images/hairstyle/MEDIUM/medium-c-curl.png', 'Y', 20);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('미디엄 레이어드', 'MEDIUM', 'F', '자연스럽게 층을 낸 중간 길이 스타일', '/images/hairstyle/MEDIUM/medium-layered.png', 'Y', 21);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('미디엄 S컬', 'MEDIUM', 'F', '부드러운 S컬을 살린 미디엄 스타일', '/images/hairstyle/MEDIUM/medium-s-curl.png', 'Y', 22);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('미디엄 울프컷', 'MEDIUM', 'F', '층과 질감이 특징인 미디엄 울프 스타일', '/images/hairstyle/MEDIUM/medium-wolf-cut.png', 'Y', 23);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('샤기컷', 'MEDIUM', 'F', '가벼운 질감과 층을 강조한 스타일', '/images/hairstyle/MEDIUM/shaggy-cut.png', 'Y', 24);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('그레이스펌', 'LONG', 'F', '우아하고 자연스러운 웨이브의 롱 펌', '/images/hairstyle/LONG/grace-perm.png', 'Y', 25);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('히피펌', 'LONG', 'F', '풍성하고 자유로운 컬이 특징인 롱 펌', '/images/hairstyle/LONG/hippie-perm.png', 'Y', 26);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('롱 C컬', 'LONG', 'F', '긴 머리 끝부분에 C컬을 살린 스타일', '/images/hairstyle/LONG/long-c-curl.png', 'Y', 27);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('롱 허쉬컷', 'LONG', 'F', '긴 머리에 가벼운 층을 낸 허쉬 스타일', '/images/hairstyle/LONG/long-hush-cut.png', 'Y', 28);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('롱 레이어드', 'LONG', 'F', '긴 머리에 자연스러운 층을 살린 스타일', '/images/hairstyle/LONG/long-layered.png', 'Y', 29);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('롱 스트레이트', 'LONG', 'F', '깔끔하고 자연스러운 긴 생머리 스타일', '/images/hairstyle/LONG/long-straight.png', 'Y', 30);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('롱 웨이브', 'LONG', 'F', '긴 머리에 자연스러운 웨이브를 살린 스타일', '/images/hairstyle/LONG/long-wave.png', 'Y', 31);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('애즈펌', 'SHORT', 'M', '자연스러운 가르마를 살린 남성 펌', '/images/hairstyle/MEN/as-perm.png', 'Y', 32);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('버즈컷', 'SHORT', 'M', '짧고 깔끔하게 정리한 남성 스타일', '/images/hairstyle/MEN/buzz-cut.png', 'Y', 33);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('콤마헤어', 'SHORT', 'M', '앞머리를 콤마 형태로 연출한 스타일', '/images/hairstyle/MEN/comma-hair.png', 'Y', 34);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('댄디컷', 'SHORT', 'M', '단정하고 자연스러운 남성 커트', '/images/hairstyle/MEN/dandy-cut.png', 'Y', 35);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('댄디펌', 'SHORT', 'M', '댄디 스타일에 자연스러운 컬을 더한 펌', '/images/hairstyle/MEN/dandy-perm.png', 'Y', 36);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('다운펌', 'SHORT', 'M', '옆머리의 볼륨을 정돈하는 남성 펌', '/images/hairstyle/MEN/down-perm.png', 'Y', 37);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('가일컷', 'SHORT', 'M', '가르마와 볼륨을 강조한 남성 스타일', '/images/hairstyle/MEN/gile-cut.png', 'Y', 38);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('아이비리그컷', 'SHORT', 'M', '깔끔하고 짧게 정리한 남성 커트', '/images/hairstyle/MEN/ivy-league-cut.png', 'Y', 39);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('리프컷', 'MEDIUM', 'M', '앞머리와 옆머리가 자연스럽게 이어지는 리프 스타일', '/images/hairstyle/MEN/leaf-cut.png', 'Y', 40);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('파트펌', 'MEDIUM', 'M', '가르마를 중심으로 볼륨을 살린 펌', '/images/hairstyle/MEN/part-perm.png', 'Y', 41);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('포마드 헤어', 'SHORT', 'M', '깔끔하게 넘겨 연출하는 포마드 스타일', '/images/hairstyle/MEN/pomade-hair.png', 'Y', 42);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('리젠트컷', 'SHORT', 'M', '앞머리에 볼륨을 살린 깔끔한 남성 스타일', '/images/hairstyle/MEN/regent-cut.png', 'Y', 43);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('세미 리프컷', 'MEDIUM', 'M', '리프컷보다 가볍고 짧게 표현한 스타일', '/images/hairstyle/MEN/semi-leaf-cut.png', 'Y', 44);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('쉐도우펌', 'SHORT', 'M', '자연스러운 볼륨과 컬이 특징인 남성 펌', '/images/hairstyle/MEN/shadow-perm.png', 'Y', 45);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('사이드 파트컷', 'SHORT', 'M', '옆가르마를 활용한 깔끔한 남성 스타일', '/images/hairstyle/MEN/side-part-cut.png', 'Y', 46);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('슬릭백', 'MEDIUM', 'M', '머리를 뒤로 넘겨 연출하는 스타일', '/images/hairstyle/MEN/slick-back.png', 'Y', 47);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('소프트 모히칸', 'SHORT', 'M', '자연스럽게 볼륨을 세운 짧은 스타일', '/images/hairstyle/MEN/soft-mohican.png', 'Y', 48);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('스핀 스왈로펌', 'MEDIUM', 'M', '움직임이 강조된 남성 펌 스타일', '/images/hairstyle/MEN/spin-swallow-perm.png', 'Y', 49);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('투블럭', 'SHORT', 'M', '옆과 뒤를 짧게 정리한 대표적인 남성 스타일', '/images/hairstyle/MEN/two-block.png', 'Y', 50);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('보브 컬', 'SHORT', 'F', '단발에 부드러운 컬을 더한 스타일', '/images/hairstyle/WOMEN/bob-curl.png', 'Y', 51);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('빌드펌', 'MEDIUM', 'F', '자연스러운 볼륨과 컬을 살린 여성 펌', '/images/hairstyle/WOMEN/build-perm.png', 'Y', 52);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('C컬펌', 'MEDIUM', 'F', '모발 끝부분의 C컬을 살린 스타일', '/images/hairstyle/WOMEN/c-curl-perm.png', 'Y', 53);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('젤리펌', 'LONG', 'F', '풍성하고 탄력 있는 컬이 특징인 펌', '/images/hairstyle/WOMEN/jelly-perm.png', 'Y', 54);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('레이어드컷', 'LONG', 'F', '자연스러운 층을 살린 여성 커트', '/images/hairstyle/WOMEN/layered-cut.png', 'Y', 55);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('S컬펌', 'LONG', 'F', '부드러운 S컬 웨이브가 특징인 스타일', '/images/hairstyle/WOMEN/s-curl-perm.png', 'Y', 56);

INSERT INTO HAIR_STYLE
    (TITLE, CATEGORY, GENDER, DESCRIPTION, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER)
VALUES
    ('태슬컷', 'SHORT', 'F', '끝선을 깔끔하게 살린 단발 스타일', '/images/hairstyle/WOMEN/tassel-cut.png', 'Y', 57);

COMMIT;

-- Verification
SELECT COUNT(*) AS HAIR_STYLE_COUNT FROM HAIR_STYLE;
SELECT GENDER, COUNT(*) AS CNT FROM HAIR_STYLE GROUP BY GENDER ORDER BY GENDER;
SELECT CATEGORY, COUNT(*) AS CNT FROM HAIR_STYLE GROUP BY CATEGORY ORDER BY CATEGORY;
SELECT NO, TITLE, CATEGORY, GENDER, IMAGE_URL, ACTIVE_YN, DISPLAY_ORDER
FROM HAIR_STYLE
ORDER BY DISPLAY_ORDER, NO;


-- V010__link_hair_styles_to_service_menu.sql
-- 예약 화면의 CUT / PERM / COLOR / CLINIC / ETC 분류와 HAIR_STYLE을 연결합니다.
-- HAIR_STYLE.CATEGORY(SHORT/MEDIUM/LONG/ETC)는 변경하지 않습니다.
-- HAIR_STYLE 57개와 SERVICE_MENU 데이터가 먼저 존재해야 합니다.
-- NO 값을 직접 하드코딩하지 않고 IMAGE_URL + SERVICE_MENU.CATEGORY로 연결합니다.
-- 같은 CATEGORY에 활성 SERVICE_MENU가 여러 개 있으면 해당 카테고리의 모든 활성 메뉴와 연결됩니다.
-- NOT EXISTS가 있어 같은 관계를 다시 실행해도 중복 INSERT되지 않습니다.
-- 현재 CLINIC에 연결할 헤어스타일 이미지는 없습니다.

-- 실행 전 확인
SELECT NO, CATEGORY, NAME, ACTIVE_YN
FROM SERVICE_MENU
ORDER BY CATEGORY, DISPLAY_ORDER, NO;

SELECT COUNT(*) AS HAIR_STYLE_COUNT
FROM HAIR_STYLE;

-- 연결 INSERT
INSERT INTO HAIR_STYLE_SERVICE (
    HAIR_STYLE_NO,
    SERVICE_MENU_NO
)
SELECT
    H.NO,
    S.NO
FROM HAIR_STYLE H
JOIN (
    SELECT '/images/hairstyle/ETC/ash-brown-color-f.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '애쉬 브라운 F' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/ash-brown-color-m.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '애쉬 브라운 M' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/brown-color-f.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '브라운 F' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/brown-color-m.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '브라운 M' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/chocolate-brown-color-f.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '초콜릿 브라운 F' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/chocolate-brown-color-m.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '초콜릿 브라운 M' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/pink-brown-color-f.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '핑크 브라운 F' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/pink-brown-color-m.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '핑크 브라운 M' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/red-brown-color-f.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '레드 브라운 F' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/ETC/red-brown-color-m.png' AS IMAGE_URL, 'COLOR' AS SERVICE_CATEGORY, '레드 브라운 M' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/bixie-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '빅시컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/blunt-bob.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '블런트 보브' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/bob.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '보브컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/crop.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '크롭컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/hush-bob.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '허쉬 보브' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/pixie-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '픽시컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/SHORT/short-layered.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '숏 레이어드' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/hush-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '허쉬컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/medium-layered.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '미디엄 레이어드' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/medium-wolf-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '미디엄 울프컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/shaggy-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '샤기컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/long-hush-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '롱 허쉬컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/long-layered.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '롱 레이어드' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/buzz-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '버즈컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/dandy-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '댄디컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/gile-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '가일컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/ivy-league-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '아이비리그컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/leaf-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '리프컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/regent-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '리젠트컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/semi-leaf-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '세미 리프컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/side-part-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '사이드 파트컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/soft-mohican.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '소프트 모히칸' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/two-block.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '투블럭' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/layered-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '레이어드컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/tassel-cut.png' AS IMAGE_URL, 'CUT' AS SERVICE_CATEGORY, '태슬컷' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/build-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '미디엄 빌드펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/medium-c-curl.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '미디엄 C컬' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEDIUM/medium-s-curl.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '미디엄 S컬' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/grace-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '그레이스펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/hippie-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '히피펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/long-c-curl.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '롱 C컬' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/long-wave.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '롱 웨이브' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/as-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '애즈펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/dandy-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '댄디펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/down-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '다운펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/part-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '파트펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/shadow-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '쉐도우펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/spin-swallow-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '스핀 스왈로펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/bob-curl.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '보브 컬' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/build-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '여성 빌드펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/c-curl-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, 'C컬펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/jelly-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, '젤리펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/WOMEN/s-curl-perm.png' AS IMAGE_URL, 'PERM' AS SERVICE_CATEGORY, 'S컬펌' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/LONG/long-straight.png' AS IMAGE_URL, 'ETC' AS SERVICE_CATEGORY, '롱 스트레이트' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/comma-hair.png' AS IMAGE_URL, 'ETC' AS SERVICE_CATEGORY, '콤마헤어' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/pomade-hair.png' AS IMAGE_URL, 'ETC' AS SERVICE_CATEGORY, '포마드 헤어' AS STYLE_LABEL FROM DUAL
    UNION ALL
    SELECT '/images/hairstyle/MEN/slick-back.png' AS IMAGE_URL, 'ETC' AS SERVICE_CATEGORY, '슬릭백' AS STYLE_LABEL FROM DUAL
) M
    ON M.IMAGE_URL = H.IMAGE_URL
JOIN SERVICE_MENU S
    ON S.CATEGORY = M.SERVICE_CATEGORY
   AND S.ACTIVE_YN = 'Y'
WHERE H.ACTIVE_YN = 'Y'
  AND NOT EXISTS (
      SELECT 1
      FROM HAIR_STYLE_SERVICE HS
      WHERE HS.HAIR_STYLE_NO = H.NO
        AND HS.SERVICE_MENU_NO = S.NO
  );

COMMIT;

-- 카테고리별 연결 확인
SELECT
    S.CATEGORY,
    COUNT(*) AS LINK_COUNT,
    COUNT(DISTINCT HS.HAIR_STYLE_NO) AS HAIR_STYLE_COUNT
FROM HAIR_STYLE_SERVICE HS
JOIN SERVICE_MENU S
    ON S.NO = HS.SERVICE_MENU_NO
GROUP BY S.CATEGORY
ORDER BY S.CATEGORY;

-- 상세 연결 확인
SELECT
    H.NO AS HAIR_STYLE_NO,
    H.TITLE,
    H.GENDER,
    H.CATEGORY AS HAIR_LENGTH_CATEGORY,
    S.CATEGORY AS SERVICE_CATEGORY,
    S.NAME AS SERVICE_MENU_NAME,
    H.IMAGE_URL
FROM HAIR_STYLE_SERVICE HS
JOIN HAIR_STYLE H
    ON H.NO = HS.HAIR_STYLE_NO
JOIN SERVICE_MENU S
    ON S.NO = HS.SERVICE_MENU_NO
ORDER BY S.CATEGORY, H.DISPLAY_ORDER, H.NO;

-- 연결되지 않은 활성 헤어스타일 확인
SELECT
    H.NO,
    H.TITLE,
    H.GENDER,
    H.CATEGORY,
    H.IMAGE_URL
FROM HAIR_STYLE H
WHERE H.ACTIVE_YN = 'Y'
  AND NOT EXISTS (
      SELECT 1
      FROM HAIR_STYLE_SERVICE HS
      WHERE HS.HAIR_STYLE_NO = H.NO
  )
ORDER BY H.DISPLAY_ORDER, H.NO;

-- 활성 서비스 메뉴와 대표 자재 연결
INSERT INTO SERVICE_MATERIAL (SERVICE_MENU_NO, MATERIAL_NO, USAGE_QUANTITY)
SELECT S.NO, M.MATERIAL_NO,
       CASE S.CATEGORY
           WHEN 'CUT' THEN 10
           WHEN 'PERM' THEN 80
           WHEN 'COLOR' THEN 60
           WHEN 'CLINIC' THEN 50
           ELSE 15
       END
FROM SERVICE_MENU S
JOIN MATERIAL M
  ON M.MATERIAL_NAME = CASE S.CATEGORY
      WHEN 'PERM' THEN '펌제'
      WHEN 'COLOR' THEN '컬러제'
      WHEN 'CLINIC' THEN '클리닉 트리트먼트'
      ELSE '기본 샴푸'
  END
WHERE S.ACTIVE_YN = 'Y'
  AND NOT EXISTS (
      SELECT 1 FROM SERVICE_MATERIAL SM
      WHERE SM.SERVICE_MENU_NO = S.NO
        AND SM.MATERIAL_NO = M.MATERIAL_NO
  );

COMMIT;

-- 최종 기대값: HAIR_STYLE 57, SERVICE_MENU 16,
-- HAIR_STYLE_SERVICE 57개 이상, SERVICE_MATERIAL 16
SELECT COUNT(*) AS HAIR_STYLE_COUNT FROM HAIR_STYLE;
SELECT COUNT(*) AS SERVICE_MENU_COUNT FROM SERVICE_MENU;
SELECT COUNT(*) AS HAIR_STYLE_SERVICE_COUNT FROM HAIR_STYLE_SERVICE;
SELECT COUNT(*) AS SERVICE_MATERIAL_COUNT FROM SERVICE_MATERIAL;
SELECT COUNT(*) AS SITE_SETTING_COUNT FROM SITE_SETTING;


-- =====================================================================
-- EVENT DISCOUNT DEMO DATA
-- 쿠폰 대신 예약 시 자동 적용되는 이벤트 할인 예시
-- =====================================================================

INSERT INTO SALON_EVENT (
    EVENT_TITLE, EVENT_CONTENT, EVENT_TYPE, EVENT_IMAGE_URL,
    TARGET_CATEGORY, DISCOUNT_TYPE, DISCOUNT_VALUE,
    MIN_PAYMENT_AMOUNT, MAX_DISCOUNT_AMOUNT,
    START_DATE, END_DATE, USE_YN
) VALUES (
    '첫 방문 50% 자동 할인',
    '첫 방문 고객에게 전체 시술 50% 할인이 자동 적용됩니다.',
    'FIRST_VISIT', NULL,
    'ALL', 'RATE', 50,
    0, NULL,
    SYSTIMESTAMP - INTERVAL '30' DAY,
    SYSTIMESTAMP + INTERVAL '365' DAY,
    'Y'
);

INSERT INTO SALON_EVENT (
    EVENT_TITLE, EVENT_CONTENT, EVENT_TYPE, EVENT_IMAGE_URL,
    TARGET_CATEGORY, DISCOUNT_TYPE, DISCOUNT_VALUE,
    MIN_PAYMENT_AMOUNT, MAX_DISCOUNT_AMOUNT,
    START_DATE, END_DATE, USE_YN
) VALUES (
    '펌 시즌 20% 할인',
    '이벤트 기간 동안 펌 시술에 20% 할인이 자동 적용됩니다.',
    'SEASON', NULL,
    'PERM', 'RATE', 20,
    0, 30000,
    SYSTIMESTAMP - INTERVAL '30' DAY,
    SYSTIMESTAMP + INTERVAL '90' DAY,
    'Y'
);

COMMIT;
