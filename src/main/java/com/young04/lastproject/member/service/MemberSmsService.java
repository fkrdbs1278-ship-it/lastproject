package com.young04.lastproject.member.service;

import com.young04.lastproject.global.sms.sender.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSmsService {

    private final SmsSender smsSender;


    /* 회원 인증번호 문자 발송 */

    public void sendVerificationCode(
            String phone,
            String verificationCode
    ) {

        String content =
                "[SALON CRM] 인증번호: "
                        + verificationCode
                        + " (3분 이내 입력)";


        /*
         * to      = 사용자 휴대전화번호
         * subject = SMS이므로 제목 없음
         * content = 실제 문자 내용
         */
        smsSender.send(
                phone,
                null,
                content
        );
    }
}