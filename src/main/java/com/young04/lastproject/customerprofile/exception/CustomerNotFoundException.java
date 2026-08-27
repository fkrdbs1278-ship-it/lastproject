package com.young04.lastproject.customerprofile.exception;

/**
 * 고객 CRM에서 요청한 고객을 찾을 수 없을 때 발생시키는 예외입니다.
 *
 * 예:
 * - 존재하지 않는 CUSTOMER_ID로 상세 조회
 * - 존재하지 않는 고객의 등급 변경
 * - 존재하지 않는 고객의 자동 등급 계산
 *
 * Controller에서 직접 if문으로 처리하지 않고
 * Service에서 이 예외를 발생시킨 뒤,
 * 이후 @ControllerAdvice에서 공통으로 처리합니다.
 */
public class CustomerNotFoundException extends RuntimeException {

    /**
     * 고객 번호를 전달받아
     * 일관된 오류 메시지를 생성합니다.
     *
     * @param customerId 존재하지 않는 고객 번호
     */
    public CustomerNotFoundException(Long customerId) {

        super(
                "고객을 찾을 수 없습니다. customerId="
                        + customerId
        );
    }
}