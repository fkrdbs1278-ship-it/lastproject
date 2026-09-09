package com.young04.lastproject.siteadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// 사용자 메인 화면에 적용할 관리자 설정값을 저장하는 Entity
@Entity
@Table(name = "SITE_SETTING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSetting {

    // 설정 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SITE_SETTING_NO")
    private Long siteSettingNo;


    // 메인 화면 제목
    @Column(name = "HERO_TITLE", length = 200)
    private String heroTitle;


    // 메인 화면 설명
    @Column(name = "HERO_DESCRIPTION", length = 500)
    private String heroDescription;


    // 메인 이미지 경로
    @Column(name = "HERO_IMAGE_URL", length = 500)
    private String heroImageUrl;


    // 서비스 안내 영역 노출 여부
    @Column(name = "SERVICE_VISIBLE", length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String serviceVisibleYn;


    // 서비스 안내 영역 제목
    @Column(name = "SERVICE_TITLE", length = 100)
    private String serviceTitle;


    // 헤어스타일 영역 노출 여부
    @Column(name = "STYLE_VISIBLE", length = 1)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String styleVisibleYn;


    // 헤어스타일 영역 제목
    @Column(name = "STYLE_TITLE", length = 100)
    private String styleTitle;


    // 헤어스타일 영역 설명
    @Column(name = "STYLE_DESCRIPTION", length = 500)
    private String styleDescription;


    // 마지막 수정 시간
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;


    // 저장 또는 수정 직전에 수정 시간을 현재 시간으로 갱신
    @PrePersist
    @PreUpdate
    private void updateTime() {
        this.updatedAt = LocalDateTime.now();
    }


    // 서비스 안내 영역 노출 여부를 boolean으로 반환
    public boolean isServiceVisible() {
        return "Y".equalsIgnoreCase(serviceVisibleYn);
    }


    // 헤어스타일 영역 노출 여부를 boolean으로 반환
    public boolean isStyleVisible() {
        return "Y".equalsIgnoreCase(styleVisibleYn);
    }
}