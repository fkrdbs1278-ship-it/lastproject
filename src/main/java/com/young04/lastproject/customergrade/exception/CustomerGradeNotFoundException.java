package com.young04.lastproject.customergrade.exception;

/**
 * 고객 등급 정보를 찾을 수 없을 때 발생시키는 예외입니다.
 *
 * 예:
 * - 존재하지 않는 등급 코드로 고객 등급 변경
 * - CUSTOMER_GRADE 테이블에
 *   NORMAL / REGULAR / VIP 데이터가 없는 경우
 *
 * Service에서 이 예외를 발생시키고
 * @ControllerAdvice에서 공통으로 처리하도록 사용합니다.
 */
public class CustomerGradeNotFoundException extends RuntimeException {

    /**
     * 조회하려던 등급 코드를 전달받아
     * 예외 메시지를 생성합니다.
     *
     * @param gradeCode 찾을 수 없는 고객 등급 코드
     */
    public CustomerGradeNotFoundException(String gradeCode) {

        super(
                "고객 등급을 찾을 수 없습니다. gradeCode="
                        + gradeCode
        );
    }
}