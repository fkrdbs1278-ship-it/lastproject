package com.young04.lastproject.review.repository;

import com.young04.lastproject.review.entity.Review;
import com.young04.lastproject.review.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {


    /* =========================================================
       사용자에게 보여줄 활성 리뷰 전체

       최신순
    ========================================================= */

    List<Review>
    findByStatusOrderByRegdateDesc(
            ReviewStatus status
    );


    /* =========================================================
       활성 리뷰 상세
    ========================================================= */

    Optional<Review>
    findByNoAndStatus(
            Long no,
            ReviewStatus status
    );


    /* =========================================================
       특정 회원의 활성 리뷰

       마이페이지에서 사용 예정
    ========================================================= */

    List<Review>
    findByMember_NoAndStatusOrderByRegdateDesc(
            Long memberNo,
            ReviewStatus status
    );


    /* =========================================================
       예약에 이미 리뷰가 있는지 확인

       예약당 리뷰 1개 정책에 사용 예정
    ========================================================= */

    boolean existsByReservationNoAndStatus(
            Long reservationNo,
            ReviewStatus status
    );

    /* =========================================================
       특정 회원이 작성한 특정 활성 리뷰

       수정 / 삭제 권한 확인용
    ========================================================= */

    Optional<Review>
    findByNoAndMember_NoAndStatus(
            Long no,
            Long memberNo,
            ReviewStatus status
    );




}