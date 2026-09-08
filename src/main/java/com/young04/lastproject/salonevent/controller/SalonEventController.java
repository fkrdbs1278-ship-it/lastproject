package com.young04.lastproject.salonevent.controller;

import com.young04.lastproject.salonevent.dto.SalonEventRequest;
import com.young04.lastproject.salonevent.service.SalonEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 관리자 이벤트 화면 요청과 응답을 처리하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/salonevent")
public class SalonEventController {

    private final SalonEventService salonEventService;

    // 전체·사용 여부별·검색어별 이벤트 목록 조회
    @GetMapping
    public String list(
            @RequestParam(required = false) String useYn,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        model.addAttribute(
                "events",
                salonEventService.getEvents(useYn, keyword)
        );

        // 검색 후에도 선택한 검색 조건을 화면에 유지
        model.addAttribute("useYn", useYn);
        model.addAttribute("keyword", keyword);

        return "salonevent/list";
    }

    // 이벤트 검색 자동완성에서 사용할 이벤트명 목록 전달
    @GetMapping("/searchsuggestions")
    @ResponseBody
    public List<String> eventTitleSuggestions() {
        return salonEventService.getEventTitleSuggestions();
    }

    // 신규 이벤트 등록 화면
    @GetMapping("/new")
    public String createForm(Model model) {
        SalonEventRequest eventRequest =
                new SalonEventRequest();

        eventRequest.setUseYn("Y");

        model.addAttribute(
                "eventRequest",
                eventRequest
        );

        model.addAttribute("formMode", "create");

        return "salonevent/form";
    }

    // 신규 이벤트 등록 처리
    @PostMapping("/new")
    public String create(
            @Valid
            @ModelAttribute("eventRequest")
            SalonEventRequest eventRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");

            return "salonevent/form";
        }

        try {
            Long eventNo =
                    salonEventService.createEvent(eventRequest);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "이벤트가 등록되었습니다."
            );

            return "redirect:/admin/salonevent/" + eventNo;

        } catch (IllegalArgumentException exception) {
            // 서비스에서 검사한 이벤트 기간 오류를 화면에 표시
            bindingResult.reject(
                    "eventPeriod",
                    exception.getMessage()
            );

            model.addAttribute("formMode", "create");

            return "salonevent/form";
        }
    }

    // 이벤트 상세 화면
    @GetMapping("/{eventNo}")
    public String detail(
            @PathVariable Long eventNo,
            Model model
    ) {
        model.addAttribute(
                "event",
                salonEventService.getEvent(eventNo)
        );

        return "salonevent/detail";
    }

    // 기존 이벤트 수정 화면
    @GetMapping("/{eventNo}/edit")
    public String updateForm(
            @PathVariable Long eventNo,
            Model model
    ) {
        model.addAttribute(
                "eventRequest",
                salonEventService.getEventForEdit(eventNo)
        );

        model.addAttribute("eventNo", eventNo);
        model.addAttribute("formMode", "edit");

        return "salonevent/form";
    }

    // 기존 이벤트 수정 처리
    @PostMapping("/{eventNo}/edit")
    public String update(
            @PathVariable Long eventNo,
            @Valid
            @ModelAttribute("eventRequest")
            SalonEventRequest eventRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("eventNo", eventNo);
            model.addAttribute("formMode", "edit");

            return "salonevent/form";
        }

        try {
            salonEventService.updateEvent(
                    eventNo,
                    eventRequest
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "이벤트가 수정되었습니다."
            );

            return "redirect:/admin/salonevent/" + eventNo;

        } catch (IllegalArgumentException exception) {
            // 서비스에서 검사한 이벤트 기간 오류를 화면에 표시
            bindingResult.reject(
                    "eventPeriod",
                    exception.getMessage()
            );

            model.addAttribute("eventNo", eventNo);
            model.addAttribute("formMode", "edit");

            return "salonevent/form";
        }
    }

    // 이벤트를 사용자 화면에서 사용 중지
    @PostMapping("/{eventNo}/stop")
    public String stop(
            @PathVariable Long eventNo,
            RedirectAttributes redirectAttributes
    ) {
        salonEventService.stopEvent(eventNo);

        redirectAttributes.addFlashAttribute(
                "message",
                "이벤트가 사용 중지되었습니다."
        );

        return "redirect:/admin/salonevent/" + eventNo;
    }

    // 사용 중지된 이벤트를 다시 노출
    @PostMapping("/{eventNo}/resume")
    public String resume(
            @PathVariable Long eventNo,
            RedirectAttributes redirectAttributes
    ) {
        salonEventService.resumeEvent(eventNo);

        redirectAttributes.addFlashAttribute(
                "message",
                "이벤트가 다시 사용 상태로 변경되었습니다."
        );

        return "redirect:/admin/salonevent/" + eventNo;
    }
}
