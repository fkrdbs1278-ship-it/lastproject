package com.young04.lastproject.servicemenu.service;

import com.young04.lastproject.servicemenu.dto.ServiceMenuAdminForm;
import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import com.young04.lastproject.servicemenu.exception.ServiceMenuNotFoundException;
import com.young04.lastproject.servicemenu.repository.ServiceMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceMenuAdminService {

    private final ServiceMenuRepository serviceMenuRepository;

    public List<ServiceMenu> getMenus(
            ServiceMenuCategory category,
            String activeYn,
            String keyword
    ) {
        String normalizedActiveYn = normalizeActiveYn(activeYn);
        String normalizedKeyword = normalizeNullable(keyword);

        return serviceMenuRepository.findAllByOrderByDisplayOrderAscNoAsc()
                .stream()
                .filter(menu -> category == null || menu.getCategory() == category)
                .filter(menu -> normalizedActiveYn == null
                        || normalizedActiveYn.equals(menu.getActiveYn()))
                .filter(menu -> normalizedKeyword == null
                        || containsIgnoreCase(menu.getName(), normalizedKeyword)
                        || containsIgnoreCase(menu.getDescription(), normalizedKeyword))
                .toList();
    }

    public ServiceMenu getMenu(Long no) {
        return serviceMenuRepository.findById(no)
                .orElseThrow(ServiceMenuNotFoundException::new);
    }

    @Transactional
    public Long create(ServiceMenuAdminForm form) {
        ServiceMenu menu = ServiceMenu.create(
                form.getCategory(),
                normalizeRequired(form.getName()),
                normalizeNullable(form.getDescription()),
                form.getPrice(),
                form.getDurationMin(),
                normalizeNullable(form.getImageUrl()),
                form.getActiveYn(),
                form.getDisplayOrder()
        );
        return serviceMenuRepository.save(menu).getNo();
    }

    @Transactional
    public void update(Long no, ServiceMenuAdminForm form) {
        ServiceMenu menu = getMenu(no);
        menu.update(
                form.getCategory(),
                normalizeRequired(form.getName()),
                normalizeNullable(form.getDescription()),
                form.getPrice(),
                form.getDurationMin(),
                normalizeNullable(form.getImageUrl()),
                form.getActiveYn(),
                form.getDisplayOrder()
        );
    }

    @Transactional
    public String toggleActive(Long no) {
        ServiceMenu menu = getMenu(no);
        String next = "Y".equals(menu.getActiveYn()) ? "N" : "Y";
        menu.changeActiveYn(next);
        return next;
    }

    public long countAll() {
        return serviceMenuRepository.count();
    }

    public long countActive() {
        return serviceMenuRepository.findByActiveYnOrderByDisplayOrderAscNoAsc("Y").size();
    }

    private static String normalizeActiveYn(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        return ("Y".equals(upper) || "N".equals(upper)) ? upper : null;
    }

    private static String normalizeRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean containsIgnoreCase(String source, String keyword) {
        return source != null
                && source.toLowerCase(Locale.ROOT)
                .contains(keyword.toLowerCase(Locale.ROOT));
    }
}
