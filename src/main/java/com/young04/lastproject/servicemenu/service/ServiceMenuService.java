package com.young04.lastproject.servicemenu.service;

import com.young04.lastproject.servicemenu.dto.ServiceMenuResponse;
import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import com.young04.lastproject.servicemenu.exception.ServiceMenuNotFoundException;
import com.young04.lastproject.servicemenu.repository.ServiceMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceMenuService {

    private final ServiceMenuRepository serviceMenuRepository;


    /* =========================================================
       사용자용 시술 메뉴 목록

       category == null
       -> 전체 조회

       category 존재
       -> 카테고리별 조회

       공통:
       ACTIVE_YN = Y만 조회
    ========================================================= */

    public List<ServiceMenuResponse> getServiceMenus(
            ServiceMenuCategory category
    ) {

        List<ServiceMenu> serviceMenus;


        if (category == null) {

            serviceMenus =
                    serviceMenuRepository
                            .findByActiveYnOrderByDisplayOrderAscNoAsc(
                                    "Y"
                            );

        } else {

            serviceMenus =
                    serviceMenuRepository
                            .findByCategoryAndActiveYnOrderByDisplayOrderAscNoAsc(
                                    category,
                                    "Y"
                            );
        }


        return serviceMenus
                .stream()
                .map(ServiceMenuResponse::from)
                .toList();
    }


    /* =========================================================
       사용자용 시술 메뉴 상세

       ACTIVE_YN = Y인 메뉴만 조회 가능
    ========================================================= */

    public ServiceMenuResponse getServiceMenu(
            Long no
    ) {

        ServiceMenu serviceMenu =
                serviceMenuRepository
                        .findByNoAndActiveYn(
                                no,
                                "Y"
                        )
                        .orElseThrow(
                                ServiceMenuNotFoundException::new
                        );


        return ServiceMenuResponse.from(
                serviceMenu
        );
    }
}
