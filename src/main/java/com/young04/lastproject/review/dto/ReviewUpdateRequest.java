package com.young04.lastproject.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateRequest {

    @NotNull(
            message = "평점을 선택해주세요."
    )
    @Min(
            value = 1,
            message = "평점은 최소 1점 이상이어야 합니다."
    )
    @Max(
            value = 5,
            message = "평점은 최대 5점까지 입력할 수 있습니다."
    )
    private Integer rating;


    @Size(
            max = 200,
            message = "제목은 200자 이하로 입력해주세요."
    )
    private String title;


    @NotBlank(
            message = "리뷰 내용을 입력해주세요."
    )
    @Size(
            max = 2000,
            message = "리뷰 내용은 2000자 이하로 입력해주세요."
    )
    private String content;
}
