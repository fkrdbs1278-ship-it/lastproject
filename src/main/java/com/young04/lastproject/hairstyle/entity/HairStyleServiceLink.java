package com.young04.lastproject.hairstyle.entity;


import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "HAIR_STYLE_SERVICE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HairStyleServiceLink {

    /* PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* HairStyle

    HAIR_STYLE_NO -> HAIR_STYLE.NO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "HAIR_STYLE_NO",
            nullable = false
    )
    private HairStyle hairStyle;


    /* ServiceMenu
        SERVICE_MENU_NO -> SERVICE_MENU.NO
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SERVICE_MENU_NO",
            nullable = false
    )
    private ServiceMenu serviceMenu;


    /* 등록일 */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;





}
