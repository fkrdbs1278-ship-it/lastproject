package com.young04.lastproject.servicemenu.repository;

import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceMenuRepository
        extends JpaRepository<ServiceMenu, Long> {


    /* =========================================================
       사용자 시술 메뉴 전체 조회

       ACTIVE_YN = Y인 시술만 조회
       DISPLAY_ORDER → NO 순서로 정렬
    ========================================================= */

    List<ServiceMenu>
    findByActiveYnOrderByDisplayOrderAscNoAsc(
            String activeYn
    );


    /* =========================================================
       카테고리별 조회

       예:
       CUT + Y
       PERM + Y
    ========================================================= */

    List<ServiceMenu>
    findByCategoryAndActiveYnOrderByDisplayOrderAscNoAsc(
            ServiceMenuCategory category,
            String activeYn
    );


    /* =========================================================
       사용자용 상세 조회

       비활성화된 시술은 조회하지 않음
    ========================================================= */

    Optional<ServiceMenu>
    findByNoAndActiveYn(
            Long no,
            String activeYn
    );
}
