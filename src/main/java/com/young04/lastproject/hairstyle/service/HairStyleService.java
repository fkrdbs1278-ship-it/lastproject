package com.young04.lastproject.hairstyle.service;

import com.young04.lastproject.hairstyle.dto.HairStyleDetailResponse;
import com.young04.lastproject.hairstyle.dto.HairStyleResponse;
import com.young04.lastproject.hairstyle.entity.HairStyle;
import com.young04.lastproject.hairstyle.entity.HairStyleGender;
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

        기존 코드 호환용 메서드

        category == null
        -> 전체

        category 존재
        -> 해당 카테고리

        gender 조건 없음
        ========================================================= */

    public List<HairStyleResponse> getHairStyles(
            HairStyleCategory category
    ) {

        return getHairStyles(
                category,
                null
        );
    }


        /* =========================================================
        사용자용 헤어스타일 목록

        category == null
        -> 모든 카테고리

        gender == null
        -> 모든 성별

        gender == F
        -> 여성 + 공용

        gender == M
        -> 남성 + 공용

        gender == ALL
        -> 공용만

        ACTIVE_YN = Y만 사용자에게 표시
        ========================================================= */

    public List<HairStyleResponse> getHairStyles(
            HairStyleCategory category,
            HairStyleGender gender
    ) {

        List<HairStyle> hairStyles;


            /* 1. 성별 조건이 없는 경우 */

        if (gender == null) {

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


            /* 2. 성별 조건 만들기 */

        List<HairStyleGender> genders;


        if (gender == HairStyleGender.F) {

            /*
             * 여성 스타일 + 공용 스타일
             */

            genders =
                    List.of(
                            HairStyleGender.F,
                            HairStyleGender.ALL
                    );

        } else if (
                gender == HairStyleGender.M
        ) {

            /*
             * 남성 스타일 + 공용 스타일
             */

            genders =
                    List.of(
                            HairStyleGender.M,
                            HairStyleGender.ALL
                    );

        } else {

            /*
             * ALL 선택 시 공용 스타일만
             */

            genders =
                    List.of(
                            HairStyleGender.ALL
                    );
        }


        /* 3. 카테고리 조건 확인 */

        if (category == null) {

            /*
             * 성별만 검색
             */

            hairStyles =
                    hairStyleRepository
                            .findByGenderInAndActiveYnOrderByDisplayOrderAscNoAsc(
                                    genders,
                                    "Y"
                            );

        } else {

            /*
             * 카테고리 + 성별 검색
             */

            hairStyles =
                    hairStyleRepository
                            .findByCategoryAndGenderInAndActiveYnOrderByDisplayOrderAscNoAsc(
                                    category,
                                    genders,
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