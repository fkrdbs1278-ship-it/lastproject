package com.young04.lastproject.member.dto.recovery;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FindIdResponse {

    /*
     * 성공 여부
     */
    private boolean success;


    /*
     * 화면에 보여줄 메시지
     */
    private String message;


    /*
     * 찾은 아이디 목록
     *
     * 예:
     * tyr***
     */
    private List<String> memberIds;
}