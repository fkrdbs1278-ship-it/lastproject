/* =========================================================
   HAIR_STYLE 이미지 확장자 변경
   PNG -> WEBP

   대상:
   IMAGE_URL이 .png / .PNG 등으로 끝나는 헤어스타일 이미지

   예:
   /images/hairstyle/bob-curl.png
   ->
   /images/hairstyle/bob-curl.webp
   ========================================================= */

UPDATE HAIR_STYLE
SET IMAGE_URL =
    REGEXP_REPLACE(
        IMAGE_URL,
        '\.png$',
        '.webp',
        1,
        0,
        'i'
    )
WHERE REGEXP_LIKE(
    IMAGE_URL,
    '\.png$',
    'i'
);