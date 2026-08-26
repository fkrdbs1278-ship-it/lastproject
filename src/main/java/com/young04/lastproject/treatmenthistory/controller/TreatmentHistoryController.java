package com.young04.lastproject.treatmenthistory.controller;

import com.young04.lastproject.treatmenthistory.dto.TreatmentHistoryResponse;
import com.young04.lastproject.treatmenthistory.service.TreatmentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/customers/{customerId}/treatments")
public class TreatmentHistoryController {

    private final TreatmentHistoryService treatmentHistoryService;


    // 고객별 시술 이력 조회
    @GetMapping
    public String treatmentList(
            @PathVariable Long customerId,
            Model model
    ) {

        List<TreatmentHistoryResponse> treatments =
                treatmentHistoryService
                        .findByCustomerId(customerId)
                        .stream()
                        .map(TreatmentHistoryResponse::from)
                        .toList();

        log.info(
                "고객 시술 이력 화면 조회 customerId={}, count={}",
                customerId,
                treatments.size()
        );

        model.addAttribute("customerId", customerId);
        model.addAttribute("treatments", treatments);

        return "customer/treatment";
    }
}