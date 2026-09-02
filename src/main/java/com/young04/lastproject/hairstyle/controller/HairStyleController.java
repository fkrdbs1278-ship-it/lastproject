package com.young04.lastproject.hairstyle.controller;

import com.young04.lastproject.hairstyle.dto.HairStyleDetailResponse;
import com.young04.lastproject.hairstyle.dto.HairStyleResponse;
import com.young04.lastproject.hairstyle.entity.HairStyleCategory;
import com.young04.lastproject.hairstyle.service.HairStyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hairstyles")
public class HairStyleController {

    private final HairStyleService hairStyleService;


    /* =========================================================
       헤어스타일 목록

       /hairstyles

       /hairstyles?category=SHORT
       /hairstyles?category=MEDIUM
       /hairstyles?category=LONG
       /hairstyles?category=MEN
       /hairstyles?category=ETC
    ========================================================= */

    @GetMapping
    public String list(

            @RequestParam(
                    name = "category",
                    required = false
            )
            HairStyleCategory category,

            Model model
    ) {

        List<HairStyleResponse> hairStyles =
                hairStyleService
                        .getHairStyles(category);


        model.addAttribute(
                "hairStyles",
                hairStyles
        );


        model.addAttribute(
                "selectedCategory",
                category
        );


        return "hairstyle/list";
    }


    /* =========================================================
       헤어스타일 상세

       예:
       /hairstyles/1
    ========================================================= */

    @GetMapping("/{no}")
    public String detail(

            @PathVariable("no")
            Long no,

            Model model
    ) {

        HairStyleDetailResponse detail =
                hairStyleService
                        .getHairStyle(no);


        model.addAttribute(
                "detail",
                detail
        );


        return "hairstyle/detail";
    }
}