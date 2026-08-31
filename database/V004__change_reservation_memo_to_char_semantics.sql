-- ==========================================================
-- V004__change_reservation_memo_to_char_semantics.sql
--
-- 한글 등 멀티바이트 문자 입력 시
-- REQUEST_MEMO VARCHAR2(1000 BYTE)의 길이 초과 문제 해결
-- ==========================================================

ALTER TABLE RESERVATION
MODIFY (
    REQUEST_MEMO VARCHAR2(1000 CHAR)
);

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    DATA_LENGTH,
    CHAR_LENGTH,
    CHAR_USED
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'RESERVATION'
  AND COLUMN_NAME = 'REQUEST_MEMO';