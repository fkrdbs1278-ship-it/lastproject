package com.young04.lastproject.siteadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettingDto {

    // 메인 화면 제목
    private String heroTitle;

    // 메인 화면 설명
    private String heroDescription;

    // 메인 이미지 경로
    private String heroImageUrl;

    // 서비스 안내 노출 여부
    private boolean serviceVisible;

    // 서비스 안내 제목
    private String serviceTitle;

    // 헤어스타일 영역 노출 여부
    private boolean styleVisible;

    // 헤어스타일 영역 제목
    private String styleTitle;

    // 헤어스타일 영역 설명
    private String styleDescription;
}