package com.young04.lastproject.servicemenu;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceMenuAdminContractTest {

    @Test
    void 관리자_시술메뉴_등록수정_활성화_삭제_경로가_존재한다() throws Exception {
        String controller = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/servicemenu/controller/ServiceMenuAdminController.java"
        ));

        assertThat(controller).contains("@RequestMapping(\"/admin/services\")");
        assertThat(controller).contains("@GetMapping(\"/new\")");
        assertThat(controller).contains("@PostMapping(\"/{no}\")");
        assertThat(controller).contains("@PostMapping(\"/{no}/toggle-active\")");
        assertThat(controller).contains("@PostMapping(\"/{no}/delete\")");
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
    void 소요시간은_5분단위이며_30분을_정상입력할수있다() throws Exception {
        String form = Files.readString(Path.of(
                "src/main/resources/templates/admin/servicemenu/form.html"
        ));
        String dto = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/servicemenu/dto/ServiceMenuAdminForm.java"
        ));

        assertThat(form).contains("min=\"5\" max=\"1440\" step=\"5\" placeholder=\"30\"");
        assertThat(dto).contains("durationMin % 5 == 0");
        assertThat(dto).contains("소요시간은 5분 단위로 입력해주세요.");
    }

    @Test
    void 참조중인_시술메뉴는_삭제를_차단하고_미사용메뉴만_삭제한다() throws Exception {
        String list = Files.readString(Path.of(
                "src/main/resources/templates/admin/servicemenu/list.html"
        ));
        String service = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/servicemenu/service/ServiceMenuAdminService.java"
        ));

        assertThat(list).contains("/{no}/delete");
        assertThat(list).contains("table-delete-button");
        assertThat(service).contains("reservationRepository.existsByServiceMenuNo(no)");
        assertThat(service).contains("treatmentHistoryRepository.existsByServiceMenuNo(no)");
        assertThat(service).contains("hairStyleServiceLinkRepository.existsByServiceMenu_No(no)");
        assertThat(service).contains("serviceMaterialRepository.existsByServiceMenuNo(no)");
        assertThat(service).contains("serviceMenuRepository.delete(menu)");
    }
}
