package com.young04.lastproject.member.exception;

public class MemberRecoveryException
        extends RuntimeException {

    public MemberRecoveryException(
            String message
    ) {

        super(message);
    }
}