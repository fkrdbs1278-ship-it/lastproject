package com.young04.lastproject.hairstyle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "HAIR_STYLE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HairStyle {

    /* PK */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    /* 헤어스타일 제목 */
    @Column(
            name = "TITLE",
            nullable = false,
            length = 100
    )
    private String title;


    /* 카테고리 SHORT/MEDIUM/LONG/MEN/ETC
    DB에서 NULL 허용
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "CATEGORY",
            length = 30
    )
    private HairStyleCategory category;

    /* 설명*/


    @Column(
            name = "DESCRIPTION",
            length = 1000
    )
    private String description;

    /* 이미지 URL
    HAIR_STYLE에서는 이미지가 필수
     */

    @Column(
            name = "IMAGE_URL",
            nullable = false,
            length = 500
    )
    private String imageUrl;

    /*활성화 여부
        Y = 사용자에게 표시
        N = 사용자에게 숨김
    */
    @Column(
            name = "ACTIVE_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String activeYn;

    /*화면 표시 순서 */

    @Column(
            name = "DISPLAY_ORDER",
            nullable = false
    )
    private Integer displayOrder;

    /*등록일 /수정일 */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;


    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;


}
