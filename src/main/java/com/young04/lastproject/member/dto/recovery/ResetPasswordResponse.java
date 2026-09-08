package com.young04.lastproject.member.dto.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResetPasswordResponse {

    private boolean success;

    private String message;
}