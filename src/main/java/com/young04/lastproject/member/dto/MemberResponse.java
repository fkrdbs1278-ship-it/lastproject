package com.young04.lastproject.member.dto;

import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.entity.MemberRole;
import com.young04.lastproject.member.entity.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {

    private Long no;

    private String memberId;

    private String name;

    private String nickname;

    private String email;

    private String phone;

    private LocalDate birthDate;

    private String gender;

    private MemberRole role;

    private MemberStatus status;

    private LocalDateTime regdate;


    public static MemberResponse from(Member member) {

        return MemberResponse.builder()
                .no(member.getNo())
                .memberId(member.getMemberId())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phone(member.getPhone())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .role(member.getRole())
                .status(member.getStatus())
                .regdate(member.getRegdate())
                .build();
    }
}