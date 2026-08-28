package com.young04.lastproject.servicemenu.dto;

import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceMenuResponse {

    private Long no;

    private ServiceMenuCategory category;

    private String categoryName;

    private String name;

    private String description;

    private Long price;

    private Integer durationMin;

    private String imageUrl;


    /* Entity -> DTO */

    public static ServiceMenuResponse from(
            ServiceMenu serviceMenu
    ) {

        return ServiceMenuResponse.builder()

                .no(serviceMenu.getNo())

                .category(
                        serviceMenu.getCategory()
                )

                .categoryName(
                        getCategoryName(
                                serviceMenu.getCategory()
                        )
                )

                .name(
                        serviceMenu.getName()
                )

                .description(
                        serviceMenu.getDescription()
                )

                .price(
                        serviceMenu.getPrice()
                )

                .durationMin(
                        serviceMenu.getDurationMin()
                )

                .imageUrl(
                        serviceMenu.getImageUrl()
                )

                .build();
    }


    /* 카테고리 한글 표시 */

    private static String getCategoryName(
            ServiceMenuCategory category
    ) {

        return switch (category) {

            case CUT -> "커트";

            case PERM -> "펌";

            case COLOR -> "염색";

            case CLINIC -> "클리닉";

            case ETC -> "기타";
        };
    }
}
