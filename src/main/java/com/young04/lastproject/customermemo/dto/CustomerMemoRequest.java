package com.young04.lastproject.customermemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerMemoRequest {

    // COUNSELING / ALLERGY / SCALP / HAIR / GENERAL
    @NotBlank(message = "메모 유형은 필수입니다.")
    @Pattern(
            regexp = "COUNSELING|ALLERGY|SCALP|HAIR|GENERAL",
            message = "올바른 메모 유형을 선택해 주세요."
    )
    private String memoType;


    @NotBlank(message = "메모 내용을 입력해 주세요.")
    @Size(
            max = 2000,
            message = "메모 내용은 2000자 이하로 입력해 주세요."
    )
    private String memoContent;


    // Y / N
    @NotBlank(message = "중요 여부는 필수입니다.")
    @Pattern(
            regexp = "Y|N",
            message = "중요 여부는 Y 또는 N만 가능합니다."
    )
    private String importantYn;
}