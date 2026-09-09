package com.young04.lastproject.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW_IMAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    /* =========================================================
       PK
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* =========================================================
       Review

       REVIEW_IMAGE.REVIEW_NO
       →
       REVIEW.NO
    ========================================================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "REVIEW_NO",
            nullable = false
    )
    private Review review;


    /* =========================================================
       이미지 경로 / URL
    ========================================================= */

    @Column(
            name = "IMAGE_URL",
            nullable = false,
            length = 500
    )
    private String imageUrl;


    /* =========================================================
       이미지 표시 순서
    ========================================================= */

    @Column(
            name = "DISPLAY_ORDER",
            nullable = false
    )
    private Integer displayOrder;


    /* =========================================================
       등록일
    ========================================================= */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;


    @PrePersist
    protected void prePersist() {

        if (displayOrder == null) {
            displayOrder = 0;
        }

        if (regdate == null) {
            regdate = LocalDateTime.now();
        }
    }
}