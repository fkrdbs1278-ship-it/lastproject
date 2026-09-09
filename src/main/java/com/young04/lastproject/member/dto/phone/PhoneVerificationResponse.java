package com.young04.lastproject.member.dto.phone;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhoneVerificationResponse {

    /*
     * 성공 여부
     */
    private boolean success;


    /*
     * 사용자에게 보여줄 메시지
     */
    private String message;
}
