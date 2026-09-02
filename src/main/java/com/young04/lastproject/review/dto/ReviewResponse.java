package com.young04.lastproject.review.dto;

import com.young04.lastproject.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long no;

    private Long memberNo;

    private String memberName;

    private String nickname;

    private Long reservationNo;

    private Integer rating;

    private String title;

    private String content;

    private LocalDateTime regdate;

    private LocalDateTime updateDate;


    public static ReviewResponse from(
            Review review
    ) {

        return ReviewResponse.builder()
                .no(
                        review.getNo()
                )
                .memberNo(
                        review.getMember().getNo()
                )
                .memberName(
                        review.getMember().getName()
                )
                .nickname(
                        review.getMember().getNickname()
                )
                .reservationNo(
                        review.getReservationNo()
                )
                .rating(
                        review.getRating()
                )
                .title(
                        review.getTitle()
                )
                .content(
                        review.getContent()
                )
                .regdate(
                        review.getRegdate()
                )
                .updateDate(
                        review.getUpdateDate()
                )
                .build();
    }
}