package com.young04.lastproject.servicemenu.controller;

import com.young04.lastproject.servicemenu.dto.ServiceMenuResponse;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import com.young04.lastproject.servicemenu.service.ServiceMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/services")
public class ServiceMenuController {
    private final ServiceMenuService serviceMenuService;

    /* 시술 메뉴 목록
        /services
        /services?category=CUT
        /services?category=PERM
     */

    @GetMapping
    public String list(

            @RequestParam(
                    name = "category",
                    required = false
            )
            ServiceMenuCategory category,

            Model model
    ){
        List<ServiceMenuResponse> menus =
                serviceMenuService
                        .getServiceMenus(category);


        model.addAttribute(
                "menus",
                menus
        );

        model.addAttribute(
                "selectedCategory",
                category
        );

        return "servicemenu/list";

    }

    /* 시술 메뉴 상세 예: /services/1 */

    @GetMapping("/{no}")
    public String detail(

            @PathVariable("no")
            Long no,

            Model model
    ){
        ServiceMenuResponse menu =
                serviceMenuService
                        .getServiceMenu(no);

        model.addAttribute(
                "menu",
                menu
        );


        return "servicemenu/detail";
    }


}
