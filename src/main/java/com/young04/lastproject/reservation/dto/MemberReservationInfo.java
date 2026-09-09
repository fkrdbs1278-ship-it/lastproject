package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberReservationInfo {

    private Long memberNo;
    private String memberId;
    private String name;
    private String phone;
    private String maskedPhone;
}
