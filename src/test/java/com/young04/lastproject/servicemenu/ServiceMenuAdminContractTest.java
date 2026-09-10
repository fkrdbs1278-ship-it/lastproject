package com.young04.lastproject.servicemenu;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceMenuAdminContractTest {

    @Test
    void 관리자_시술메뉴_목록과_등록수정_경로가_존재한다() throws Exception {
        String controller = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/servicemenu/controller/ServiceMenuAdminController.java"
        ));

        assertThat(controller).contains("@RequestMapping(\"/admin/services\")");
        assertThat(controller).contains("@GetMapping(\"/new\")");
        assertThat(controller).contains("@PostMapping(\"/{no}\")");
        assertThat(controller).contains("@PostMapping(\"/{no}/toggle-active\")");
    }

    @Test
    void 관리자_시술메뉴_화면은_공통_대시보드_사이드바를_사용한다() throws Exception {
        String list = Files.readString(Path.of(
                "src/main/resources/templates/admin/servicemenu/list.html"
        ));
        String form = Files.readString(Path.of(
                "src/main/resources/templates/admin/servicemenu/form.html"
        ));

        assertThat(list).contains("dashboard-sidebar :: sidebar('service')");
        assertThat(form).contains("dashboard-sidebar :: sidebar('service')");
        assertThat(list).contains("/css/admin/dashboard.css");
        assertThat(form).contains("/css/admin/dashboard.css");
    }

    @Test
    void 시술메뉴는_삭제하지_않고_활성비활성으로_관리한다() throws Exception {
        String list = Files.readString(Path.of(
                "src/main/resources/templates/admin/servicemenu/list.html"
        ));
        String service = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/servicemenu/service/ServiceMenuAdminService.java"
        ));

        assertThat(list).contains("toggle-active");
        assertThat(service).contains("changeActiveYn");
        assertThat(service).doesNotContain("deleteById");
    }
}
