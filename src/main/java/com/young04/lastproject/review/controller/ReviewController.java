package com.young04.lastproject.review.controller;

import com.young04.lastproject.global.security.CustomUserDetails;
import com.young04.lastproject.review.dto.ReviewCreateRequest;
import com.young04.lastproject.review.dto.ReviewResponse;
import com.young04.lastproject.review.dto.ReviewUpdateRequest;
import com.young04.lastproject.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;


    /* =========================================================
       리뷰 목록
    ========================================================= */

    @GetMapping
    public String list(
            Model model,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {

        List<ReviewResponse> reviews =
                reviewService.getReviews();


        model.addAttribute(
                "reviews",
                reviews
        );


        /*
         * 로그인한 경우에만 현재 회원 번호 전달
         *
         * 화면에서 내 리뷰인지 판단할 때 사용
         */
        if (loginUser != null) {

            model.addAttribute(
                    "currentMemberNo",
                    loginUser.getMemberNo()
            );
        }


        return "review/list";
    }


    /* =========================================================
       리뷰 상세
    ========================================================= */

    @GetMapping("/{reviewNo}")
    public String detail(
            @PathVariable Long reviewNo,
            Model model,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {

        ReviewResponse review =
                reviewService.getReview(
                        reviewNo
                );


        model.addAttribute(
                "review",
                review
        );


        if (loginUser != null) {

            model.addAttribute(
                    "currentMemberNo",
                    loginUser.getMemberNo()
            );
        }


        return "review/detail";
    }


    /* =========================================================
       리뷰 작성 화면
    ========================================================= */

    @GetMapping("/write")
    public String writeForm(
            @RequestParam(
                    name = "reservationNo",
                    required = false
            )
            Long reservationNo,

            Model model,

            @AuthenticationPrincipal
            CustomUserDetails loginUser
    ) {

        /*
         * 로그인하지 않은 사용자는
         * 로그인 페이지로 이동
         */
        if (loginUser == null) {

            return "redirect:/member/login";
        }


        ReviewCreateRequest request =
                new ReviewCreateRequest();


        /*
         * 현재는 선택값.
         *
         * STEP 9-3에서 예약 기능과 연결되면
         * 예약 페이지에서 reservationNo를 전달하게 된다.
         */
        request.setReservationNo(
                reservationNo
        );


        model.addAttribute(
                "reviewCreateRequest",
                request
        );


        return "review/write";
    }


    /* =========================================================
       리뷰 등록
    ========================================================= */

    @PostMapping("/write")
    public String write(
            @Valid
            @ModelAttribute("reviewCreateRequest")
            ReviewCreateRequest request,

            BindingResult bindingResult,

            @AuthenticationPrincipal
            CustomUserDetails loginUser,

            RedirectAttributes redirectAttributes
    ) {

        if (loginUser == null) {

            return "redirect:/member/login";
        }


        if (bindingResult.hasErrors()) {

            return "review/write";
        }


        Long reviewNo =
                reviewService.createReview(
                        loginUser.getMemberNo(),
                        request
                );


        redirectAttributes.addFlashAttribute(
                "message",
                "리뷰가 등록되었습니다."
        );


        return "redirect:/reviews/" + reviewNo;
    }


    /* =========================================================
       리뷰 수정 화면
    ========================================================= */

    @GetMapping("/{reviewNo}/edit")
    public String editForm(
            @PathVariable Long reviewNo,
            Model model,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {

        if (loginUser == null) {

            return "redirect:/member/login";
        }


        ReviewResponse review =
                reviewService.getMyReview(
                        loginUser.getMemberNo(),
                        reviewNo
                );


        ReviewUpdateRequest request =
                new ReviewUpdateRequest();


        request.setRating(
                review.getRating()
        );

        request.setTitle(
                review.getTitle()
        );

        request.setContent(
                review.getContent()
        );


        model.addAttribute(
                "reviewNo",
                reviewNo
        );

        model.addAttribute(
                "reviewUpdateRequest",
                request
        );


        return "review/edit";
    }


    /* =========================================================
       리뷰 수정 처리
    ========================================================= */

    @PostMapping("/{reviewNo}/edit")
    public String edit(
            @PathVariable Long reviewNo,

            @Valid
            @ModelAttribute("reviewUpdateRequest")
            ReviewUpdateRequest request,

            BindingResult bindingResult,

            Model model,

            @AuthenticationPrincipal
            CustomUserDetails loginUser,

            RedirectAttributes redirectAttributes
    ) {

        if (loginUser == null) {

            return "redirect:/member/login";
        }


        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "reviewNo",
                    reviewNo
            );

            return "review/edit";
        }


        reviewService.updateReview(
                loginUser.getMemberNo(),
                reviewNo,
                request
        );


        redirectAttributes.addFlashAttribute(
                "message",
                "리뷰가 수정되었습니다."
        );


        return "redirect:/reviews/" + reviewNo;
    }


    /* =========================================================
       리뷰 삭제

       실제 DELETE가 아니라
       STATUS = DELETED
    ========================================================= */

    @PostMapping("/{reviewNo}/delete")
    public String delete(
            @PathVariable Long reviewNo,

            @AuthenticationPrincipal
            CustomUserDetails loginUser,

            RedirectAttributes redirectAttributes
    ) {

        if (loginUser == null) {

            return "redirect:/member/login";
        }


        reviewService.deleteReview(
                loginUser.getMemberNo(),
                reviewNo
        );


        redirectAttributes.addFlashAttribute(
                "message",
                "리뷰가 삭제되었습니다."
        );


        return "redirect:/reviews";
    }
}
