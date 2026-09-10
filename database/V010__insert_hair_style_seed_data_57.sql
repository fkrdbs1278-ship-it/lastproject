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