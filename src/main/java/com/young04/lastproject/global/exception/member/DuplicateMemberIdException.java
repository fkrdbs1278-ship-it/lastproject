package com.young04.lastproject.global.exception.member;

public class DuplicateMemberIdException extends RuntimeException {

    public DuplicateMemberIdException() {
        super("이미 사용 중인 아이디입니다.");
    }
}
