package com.young04.lastproject.hairstyle.service;

import com.young04.lastproject.hairstyle.dto.HairStyleDetailResponse;
import com.young04.lastproject.hairstyle.dto.HairStyleResponse;
import com.young04.lastproject.hairstyle.entity.HairStyle;
import com.young04.lastproject.hairstyle.entity.HairStyleCategory;
import com.young04.lastproject.hairstyle.entity.HairStyleServiceLink;
import com.young04.lastproject.hairstyle.exception.HairStyleNotFoundException;
import com.young04.lastproject.hairstyle.repository.HairStyleRepository;
import com.young04.lastproject.hairstyle.repository.HairStyleServiceLinkRepository;
import com.young04.lastproject.servicemenu.dto.ServiceMenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HairStyleService {

    private final HairStyleRepository hairStyleRepository;

    private final HairStyleServiceLinkRepository
            hairStyleServiceLinkRepository;


    /* =========================================================
       사용자용 헤어스타일 목록

       category == null
       -> 전체

       category 존재
       -> 해당 카테고리

       ACTIVE_YN = Y만 사용자에게 표시
    ========================================================= */

    public List<HairStyleResponse> getHairStyles(
            HairStyleCategory category
    ) {

        List<HairStyle> hairStyles;


        if (category == null) {

            hairStyles =
                    hairStyleRepository
                            .findByActiveYnOrderByDisplayOrderAscNoAsc(
                                    "Y"
                            );

        } else {

            hairStyles =
                    hairStyleRepository
                            .findByCategoryAndActiveYnOrderByDisplayOrderAscNoAsc(
                                    category,
                                    "Y"
                            );
        }


        return hairStyles
                .stream()
                .map(HairStyleResponse::from)
                .toList();
    }


    /* =========================================================
       사용자용 헤어스타일 상세

       헤어스타일 정보
       +
       연결된 시술 메뉴
    ========================================================= */

    public HairStyleDetailResponse getHairStyle(
            Long no
    ) {

        HairStyle hairStyle =
                hairStyleRepository
                        .findByNoAndActiveYn(
                                no,
                                "Y"
                        )
                        .orElseThrow(
                                HairStyleNotFoundException::new
                        );


        List<HairStyleServiceLink> links =
                hairStyleServiceLinkRepository
                        .findByHairStyle_NoOrderByServiceMenu_DisplayOrderAscServiceMenu_NoAsc(
                                no
                        );


        /*
         * HAIR_STYLE_SERVICE에 연결되어 있더라도
         * SERVICE_MENU.ACTIVE_YN = N이면
         * 사용자에게 추천 시술로 보여주지 않는다.
         */

        List<ServiceMenuResponse> recommendedServices =
                links
                        .stream()
                        .filter(
                                link ->
                                        "Y".equals(
                                                link
                                                        .getServiceMenu()
                                                        .getActiveYn()
                                        )
                        )
                        .map(
                                link ->
                                        ServiceMenuResponse.from(
                                                link.getServiceMenu()
                                        )
                        )
                        .toList();


        return HairStyleDetailResponse.builder()
                .hairStyle(
                        HairStyleResponse.from(
                                hairStyle
                        )
                )
                .recommendedServices(
                        recommendedServices
                )
                .build();
    }
}