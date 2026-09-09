package com.young04.lastproject.customerprofile.exception;

import com.young04.lastproject.customergrade.exception.CustomerGradeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class CustomerCrmExceptionAdvice {


    // =====================================================
    // 고객 조회 실패
    // =====================================================

    /**
     * 존재하지 않는 고객번호로
     * 고객 상세 / 등급 변경 등을 요청한 경우 처리합니다.
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public String handleCustomerNotFound(
            CustomerNotFoundException exception,
            RedirectAttributes redirectAttributes
    ) {

        log.warn(
                "고객 CRM 고객 조회 예외 message={}",
                exception.getMessage()
        );


        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );


        return "redirect:/admin/customers";
    }


    // =====================================================
    // 고객 등급 조회 실패
    // =====================================================

    /**
     * 존재하지 않는 등급 코드가 전달되거나
     * CUSTOMER_GRADE의 필수 데이터가 없는 경우 처리합니다.
     */
    @ExceptionHandler(CustomerGradeNotFoundException.class)
    public String handleCustomerGradeNotFound(
            CustomerGradeNotFoundException exception,
            RedirectAttributes redirectAttributes
    ) {

        log.error(
                "고객 CRM 등급 조회 예외 message={}",
                exception.getMessage()
        );


        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );


        return "redirect:/admin/customers";
    }


    // =====================================================
    // 전화번호 중복
    // =====================================================

    /**
     * 전화예약 고객 등록 시
     * 동일한 전화번호가 이미 존재하는 경우 처리합니다.
     *
     * 오류 메시지를 FlashAttribute로 전달하고
     * 다시 전화예약 고객 등록 화면으로 이동합니다.
     */
    @ExceptionHandler(DuplicateCustomerPhoneException.class)
    public String handleDuplicateCustomerPhone(
            DuplicateCustomerPhoneException exception,
            RedirectAttributes redirectAttributes
    ) {

        log.warn(
                "전화예약 고객 중복 전화번호 예외 message={}",
                exception.getMessage()
        );


        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage()
        );


        return "redirect:/admin/customers/new";
    }
}