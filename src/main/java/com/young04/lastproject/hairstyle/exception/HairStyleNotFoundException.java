package com.young04.lastproject.hairstyle.exception;

public class HairStyleNotFoundException
        extends RuntimeException {

    public HairStyleNotFoundException() {

        super("헤어스타일을 찾을 수 없습니다.");
    }
}