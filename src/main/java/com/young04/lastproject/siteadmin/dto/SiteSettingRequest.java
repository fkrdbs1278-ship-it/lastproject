package com.young04.lastproject.siteadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// 관리자 화면에서 변경한 사용자 사이트 설정값을 받는 DTO
@Getter
@Setter
@NoArgsConstructor
public class SiteSettingRequest {

    // 메인 화면 제목
    @NotBlank(message = "메인 제목을 입력해주세요.")
    @Size(max = 200, message = "메인 제목은 200자 이하로 입력해주세요.")
    private String heroTitle;


    // 메인 화면 설명
    @Size(max = 500, message = "메인 설명은 500자 이하로 입력해주세요.")
    private String heroDescription;


    // 새로 선택한 메인 이미지
    // 이미지를 변경하지 않으면 null 또는 빈 파일로 전달
    private MultipartFile heroImage;


    // 서비스 안내 영역 노출 여부
    private boolean serviceVisible;


    // 서비스 안내 영역 제목
    @NotBlank(message = "서비스 안내 제목을 입력해주세요.")
    @Size(max = 100, message = "서비스 안내 제목은 100자 이하로 입력해주세요.")
    private String serviceTitle;


    // 헤어스타일 영역 노출 여부
    private boolean styleVisible;


    // 헤어스타일 영역 제목
    @NotBlank(message = "헤어스타일 영역 제목을 입력해주세요.")
    @Size(max = 100, message = "헤어스타일 영역 제목은 100자 이하로 입력해주세요.")
    private String styleTitle;


    // 헤어스타일 영역 설명
    @Size(max = 500, message = "헤어스타일 영역 설명은 500자 이하로 입력해주세요.")
    private String styleDescription;
}