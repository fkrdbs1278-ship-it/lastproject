package com.young04.lastproject.review.repository;

import com.young04.lastproject.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository
        extends JpaRepository<ReviewImage, Long> {


    /* =========================================================
       특정 리뷰 이미지

       DISPLAY_ORDER 순서대로 조회
    ========================================================= */

    List<ReviewImage>
    findByReview_NoOrderByDisplayOrderAscNoAsc(
            Long reviewNo
    );
}