package com.young04.lastproject.servicemenu.controller;

import com.young04.lastproject.servicemenu.dto.ServiceMenuAdminForm;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import com.young04.lastproject.servicemenu.service.ServiceMenuAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/services")
public class ServiceMenuAdminController {

    private final ServiceMenuAdminService serviceMenuAdminService;

    @GetMapping
    public String list(
            @RequestParam(name = "category", required = false)
            ServiceMenuCategory category,
            @RequestParam(name = "activeYn", required = false)
            String activeYn,
            @RequestParam(name = "keyword", required = false)
            String keyword,
            Model model
    ) {
        model.addAttribute(
                "menus",
                serviceMenuAdminService.getMenus(category, activeYn, keyword)
        );
        model.addAttribute("categories", ServiceMenuCategory.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedActiveYn", activeYn);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", serviceMenuAdminService.countAll());
        model.addAttribute("activeCount", serviceMenuAdminService.countActive());
        return "admin/servicemenu/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ServiceMenuAdminForm());
        }
        addFormCommon(model, false, null);
        return "admin/servicemenu/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") ServiceMenuAdminForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormCommon(model, false, null);
            return "admin/servicemenu/form";
        }

        Long no = serviceMenuAdminService.create(form);
        redirectAttributes.addFlashAttribute("message", "시술 메뉴가 등록되었습니다.");
        return "redirect:/admin/services/" + no + "/edit";
    }

    @GetMapping("/{no}/edit")
    public String editForm(@PathVariable Long no, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute(
                    "form",
                    ServiceMenuAdminForm.from(serviceMenuAdminService.getMenu(no))
            );
        }
        addFormCommon(model, true, no);
        return "admin/servicemenu/form";
    }

    @PostMapping("/{no}")
    public String update(
            @PathVariable Long no,
            @Valid @ModelAttribute("form") ServiceMenuAdminForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormCommon(model, true, no);
            return "admin/servicemenu/form";
        }

        serviceMenuAdminService.update(no, form);
        redirectAttributes.addFlashAttribute("message", "시술 메뉴가 수정되었습니다.");
        return "redirect:/admin/services/" + no + "/edit";
    }

    @PostMapping("/{no}/toggle-active")
    public String toggleActive(
            @PathVariable Long no,
            RedirectAttributes redirectAttributes
    ) {
        String activeYn = serviceMenuAdminService.toggleActive(no);
        redirectAttributes.addFlashAttribute(
                "message",
                "Y".equals(activeYn)
                        ? "시술 메뉴를 활성화했습니다."
                        : "시술 메뉴를 비활성화했습니다."
        );
        return "redirect:/admin/services";
    }

    private void addFormCommon(Model model, boolean editing, Long menuNo) {
        model.addAttribute("categories", ServiceMenuCategory.values());
        model.addAttribute("editing", editing);
        model.addAttribute("menuNo", menuNo);
    }
}
