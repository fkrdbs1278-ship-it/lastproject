package com.young04.lastproject.customerprofile.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerSearchCondition {

    // 이름 또는 전화번호 검색
    private String keyword;

    // MEMBER / GUEST
    private String customerType;

    // NORMAL / REGULAR / VIP
    private String gradeCode;

    // Y / N
    private String activeYn;

    // 30일, 60일 등 장기 미방문 기준
    private Integer inactiveDays;
}