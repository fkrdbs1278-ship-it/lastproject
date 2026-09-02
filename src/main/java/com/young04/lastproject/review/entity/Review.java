package com.young04.lastproject.review.entity;

import com.young04.lastproject.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    /* =========================================================
       PK
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* =========================================================
       작성 회원

       REVIEW.MEMBER_NO
       →
       MEMBER.NO
    ========================================================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "MEMBER_NO",
            nullable = false
    )
    private Member member;


    /* =========================================================
       예약 번호

       현재 예약 Java 도메인과 결합하지 않기 위해
       우선 FK 값을 Long으로 매핑한다.

       SQL에서 NULL 허용
    ========================================================= */

    @Column(name = "RESERVATION_NO")
    private Long reservationNo;


    /* =========================================================
       평점

       1 ~ 5
    ========================================================= */

    @Column(
            name = "RATING",
            nullable = false
    )
    private Integer rating;


    /* =========================================================
       제목

       선택사항
       최대 200자
    ========================================================= */

    @Column(
            name = "TITLE",
            length = 200
    )
    private String title;


    /* =========================================================
       리뷰 내용

       최대 2000자
    ========================================================= */

    @Column(
            name = "CONTENT",
            nullable = false,
            length = 2000
    )
    private String content;


    /* =========================================================
       상태

       ACTIVE
       DELETED
    ========================================================= */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "STATUS",
            nullable = false,
            length = 20
    )
    private ReviewStatus status;


    /* =========================================================
       등록 / 수정일
    ========================================================= */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;


    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    /* =========================================================
    리뷰 생성

    Entity 전체에 @Builder를 붙이지 않고
    필요한 값만 받는 생성자에 Builder 사용
    ========================================================= */

    @Builder
    private Review(
            Member member,
            Long reservationNo,
            Integer rating,
            String title,
            String content
    ) {

        this.member = member;

        this.reservationNo =
                reservationNo;

        this.rating =
                rating;

        this.title =
                title;

        this.content =
                content;
    }


    /* =========================================================
    리뷰 수정
    ========================================================= */

    public void update(
            Integer rating,
            String title,
            String content
    ) {

        this.rating =
                rating;

        this.title =
                title;

        this.content =
                content;
    }


    /* =========================================================
    리뷰 삭제

    실제 DELETE가 아니라
    ACTIVE -> DELETED
    ========================================================= */

    public void delete() {

        this.status =
                ReviewStatus.DELETED;
    }


    /* =========================================================
       등록 전 기본값
    ========================================================= */

    @PrePersist
    protected void prePersist() {

        if (status == null) {
            status = ReviewStatus.ACTIVE;
        }

        if (regdate == null) {
            regdate = LocalDateTime.now();
        }
    }


    /* =========================================================
       수정일
    ========================================================= */

    @PreUpdate
    protected void preUpdate() {

        updateDate =
                LocalDateTime.now();
    }
}