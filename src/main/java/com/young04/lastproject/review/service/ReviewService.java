package com.young04.lastproject.review.service;

import com.young04.lastproject.global.exception.member.MemberNotFoundException;
import com.young04.lastproject.global.exception.review.ReviewAccessDeniedException;
import com.young04.lastproject.global.exception.review.ReviewNotFoundException;
import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import com.young04.lastproject.review.dto.ReviewCreateRequest;
import com.young04.lastproject.review.dto.ReviewResponse;
import com.young04.lastproject.review.dto.ReviewUpdateRequest;
import com.young04.lastproject.review.entity.Review;
import com.young04.lastproject.review.entity.ReviewStatus;
import com.young04.lastproject.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final MemberRepository memberRepository;


    /* =========================================================
       전체 활성 리뷰 조회

       최신순
    ========================================================= */

    public List<ReviewResponse> getReviews() {

        return reviewRepository
                .findByStatusOrderByRegdateDesc(
                        ReviewStatus.ACTIVE
                )
                .stream()
                .map(
                        ReviewResponse::from
                )
                .toList();
    }


    /* =========================================================
       리뷰 상세 조회
    ========================================================= */

    public ReviewResponse getReview(
            Long reviewNo
    ) {

        Review review =
                reviewRepository
                        .findByNoAndStatus(
                                reviewNo,
                                ReviewStatus.ACTIVE
                        )
                        .orElseThrow(
                                ReviewNotFoundException::new
                        );


        return ReviewResponse.from(
                review
        );
    }


    /* =========================================================
       내 리뷰 목록

       마이페이지에서 사용 가능
    ========================================================= */

    public List<ReviewResponse> getMyReviews(
            Long memberNo
    ) {

        return reviewRepository
                .findByMember_NoAndStatusOrderByRegdateDesc(
                        memberNo,
                        ReviewStatus.ACTIVE
                )
                .stream()
                .map(
                        ReviewResponse::from
                )
                .toList();
    }

    /* =========================================================
    내 리뷰 상세 조회

    수정 화면에서 사용
    ========================================================= */

    public ReviewResponse getMyReview(
            Long memberNo,
            Long reviewNo
    ) {

        Review review =
                findMyReview(
                        memberNo,
                        reviewNo
                );


        return ReviewResponse.from(
                review
        );
    }



    /* =========================================================
       리뷰 등록
    ========================================================= */

    @Transactional
    public Long createReview(
            Long memberNo,
            ReviewCreateRequest request
    ) {

        /* =====================================================
           회원 확인
        ===================================================== */

        Member member =
                memberRepository
                        .findById(memberNo)
                        .orElseThrow(
                                MemberNotFoundException::new
                        );


        /*
         * STEP 9-3에서 추가:
         *
         * 1. reservationNo가 본인 예약인가?
         * 2. COMPLETED 예약인가?
         * 3. 해당 예약에 ACTIVE 리뷰가 이미 존재하는가?
         */


        /* =====================================================
           Review 생성
        ===================================================== */

        Review review =
                Review.builder()
                        .member(member)
                        .reservationNo(
                                request.getReservationNo()
                        )
                        .rating(
                                request.getRating()
                        )
                        .title(
                                normalizeNullable(
                                        request.getTitle()
                                )
                        )
                        .content(
                                request
                                        .getContent()
                                        .trim()
                        )
                        .build();


        Review savedReview =
                reviewRepository.save(
                        review
                );


        return savedReview.getNo();
    }


    /* =========================================================
       리뷰 수정
    ========================================================= */

    @Transactional
    public void updateReview(
            Long memberNo,
            Long reviewNo,
            ReviewUpdateRequest request
    ) {

        Review review =
                findMyReview(
                        memberNo,
                        reviewNo
                );


        review.update(
                request.getRating(),

                normalizeNullable(
                        request.getTitle()
                ),

                request
                        .getContent()
                        .trim()
        );


        /*
         * JPA Dirty Checking으로 UPDATE
         * 별도의 save() 필요 없음
         */
    }


    /* =========================================================
       리뷰 삭제

       DELETE가 아니라
       STATUS = DELETED
    ========================================================= */

    @Transactional
    public void deleteReview(
            Long memberNo,
            Long reviewNo
    ) {

        Review review =
                findMyReview(
                        memberNo,
                        reviewNo
                );


        review.delete();


        /*
         * Dirty Checking
         *
         * UPDATE REVIEW
         * SET STATUS = 'DELETED'
         */
    }


    /* =========================================================
       로그인 회원의 리뷰인지 확인

       수정 / 삭제 공통 사용
    ========================================================= */

    private Review findMyReview(
            Long memberNo,
            Long reviewNo
    ) {

        /*
         * 우선 활성 리뷰가 존재하는지 확인
         */
        Review review =
                reviewRepository
                        .findByNoAndStatus(
                                reviewNo,
                                ReviewStatus.ACTIVE
                        )
                        .orElseThrow(
                                ReviewNotFoundException::new
                        );


        /*
         * 작성자 확인
         */
        if (!review
                .getMember()
                .getNo()
                .equals(memberNo)) {

            throw new ReviewAccessDeniedException();
        }


        return review;
    }


    /* =========================================================
       빈 문자열 -> null
    ========================================================= */

    private String normalizeNullable(
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }
}
