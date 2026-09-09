package com.young04.lastproject.hairstyle.dto;

import com.young04.lastproject.hairstyle.entity.HairStyle;
import com.young04.lastproject.hairstyle.entity.HairStyleCategory;
import com.young04.lastproject.hairstyle.entity.HairStyleGender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HairStyleResponse {

    private Long no;

    private HairStyleCategory category;

    private String categoryName;

    /* 성별 */

    private HairStyleGender gender;

    private String genderName;


    private String title;

    private String description;

    private String imageUrl;


    /* Entity -> DTO */

    public static HairStyleResponse from(
            HairStyle hairStyle
    ) {

        return HairStyleResponse.builder()
                .no(hairStyle.getNo())
                .category(hairStyle.getCategory())

                .categoryName(
                        getCategoryName(
                                hairStyle.getCategory()
                        )
                )

                .gender(
                        hairStyle.getGender()
                )

                .genderName(
                        getGenderName(
                                hairStyle.getGender()
                        )
                )

                .title(hairStyle.getTitle())
                .description(hairStyle.getDescription())
                .imageUrl(hairStyle.getImageUrl())
                .build();
    }


    /* 카테고리 한글 변환 */

    private static String getCategoryName(
            HairStyleCategory category
    ) {

        if (category == null) {
            return "미분류";
        }


        return switch (category) {

            case SHORT -> "숏";

            case MEDIUM -> "미디엄";

            case LONG -> "롱";

            case MEN -> "남성";

            case WOMEN -> "여성";

            case ETC -> "기타";
        };
    }


    /* 성별 한글 변환 */

    private static String getGenderName(
            HairStyleGender gender
    ) {

        if (gender == null) {
            return "공용";
        }


        return switch (gender) {

            case M -> "남성";

            case F -> "여성";

            case ALL -> "공용";
        };
    }
}

