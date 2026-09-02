package com.young04.lastproject.global.exception.member;

public class MemberNotFoundException
        extends RuntimeException{
    public MemberNotFoundException(){
        super("회원 정보를 찾을 수 없습니다.");
    }
}
