package com.young04.lastproject.global.exception.member;

public class InvalidBirthDateException extends RuntimeException {

    public InvalidBirthDateException() {

        super(
                "생년월일은 1900년 1월 1일 이후의 과거 날짜만 선택할 수 있습니다."
        );
    }
}