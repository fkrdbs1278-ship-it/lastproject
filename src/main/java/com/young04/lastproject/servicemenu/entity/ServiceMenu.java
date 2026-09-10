package com.young04.lastproject.servicemenu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SERVICE_MENU")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceMenu {

    /* PK */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* 시술 카테고리
       CUT / PERM / COLOR / CLINIC / ETC */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "CATEGORY",
            nullable = false,
            length = 30
    )
    private ServiceMenuCategory category;


    /* 시술명 */

    @Column(
            name = "NAME",
            nullable = false,
            length = 100
    )
    private String name;


    /* 시술 설명 */

    @Column(
            name = "DESCRIPTION",
            length = 1000
    )
    private String description;


    /* 가격 */

    @Column(
            name = "PRICE",
            nullable = false
    )
    private Long price;


    /* 예상 소요시간 (분) */

    @Column(
            name = "DURATION_MIN",
            nullable = false
    )
    private Integer durationMin;


    /* 시술 이미지 */

    @Column(
            name = "IMAGE_URL",
            length = 500
    )
    private String imageUrl;


    /* 활성화 여부
       Y = 사용자에게 표시
       N = 사용자에게 숨김 */

    @Column(
            name = "ACTIVE_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String activeYn;


    /* 화면 표시 순서 */

    @Column(
            name = "DISPLAY_ORDER",
            nullable = false
    )
    private Integer displayOrder;


    /* 등록 / 수정일 */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;


    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    public static ServiceMenu create(
            ServiceMenuCategory category,
            String name,
            String description,
            Long price,
            Integer durationMin,
            String imageUrl,
            String activeYn,
            Integer displayOrder
    ) {
        ServiceMenu menu = new ServiceMenu();
        menu.category = category;
        menu.name = name;
        menu.description = description;
        menu.price = price;
        menu.durationMin = durationMin;
        menu.imageUrl = imageUrl;
        menu.activeYn = activeYn;
        menu.displayOrder = displayOrder;
        menu.regdate = LocalDateTime.now();
        return menu;
    }

    public void update(
            ServiceMenuCategory category,
            String name,
            String description,
            Long price,
            Integer durationMin,
            String imageUrl,
            String activeYn,
            Integer displayOrder
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMin = durationMin;
        this.imageUrl = imageUrl;
        this.activeYn = activeYn;
        this.displayOrder = displayOrder;
        this.updateDate = LocalDateTime.now();
    }

    public void changeActiveYn(String activeYn) {
        this.activeYn = activeYn;
        this.updateDate = LocalDateTime.now();
    }

}
