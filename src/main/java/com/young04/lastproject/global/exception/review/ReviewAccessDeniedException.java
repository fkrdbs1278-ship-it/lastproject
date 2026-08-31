package com.young04.lastproject.global.exception.review;

public class ReviewAccessDeniedException
        extends RuntimeException {

    public ReviewAccessDeniedException() {

        super(
                "해당 리뷰를 수정하거나 삭제할 권한이 없습니다."
        );
    }
}
