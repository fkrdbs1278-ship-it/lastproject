package com.young04.lastproject.siteadmin.service;

import com.young04.lastproject.siteadmin.dto.SiteSettingDto;
import com.young04.lastproject.siteadmin.dto.SiteSettingRequest;
import com.young04.lastproject.siteadmin.entity.SiteSetting;
import com.young04.lastproject.siteadmin.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 설정값을 조회하고 사용자 메인 화면에 전달하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;


    // 현재 사용자 사이트 설정 조회
    public SiteSettingDto getCurrentSetting() {

        SiteSetting setting = siteSettingRepository
                .findTopByOrderBySiteSettingNoAsc()
                .orElseGet(this::createDefaultSetting);

        return toDto(setting);
    }


    // DB에 설정이 없을 때 사용할 기본값 생성
    private SiteSetting createDefaultSetting() {

        return SiteSetting.builder()
                .heroTitle("나에게 어울리는 스타일을 찾아보세요.")
                .heroDescription(
                        "원하는 시술과 헤어스타일을 확인하고 편리하게 예약 서비스를 이용해보세요."
                )
                .heroImageUrl("/images/hero/hero1.jpg")
                .serviceVisibleYn("Y")
                .serviceTitle("서비스 안내")
                .styleVisibleYn("Y")
                .styleTitle("헤어스타일 둘러보기")
                .styleDescription(
                        "다양한 스타일을 확인하고 원하는 헤어스타일을 찾아보세요."
                )
                .build();
    }


    // Entity 데이터를 화면에서 사용할 DTO로 변환
    private SiteSettingDto toDto(SiteSetting setting) {

        return SiteSettingDto.builder()
                .heroTitle(setting.getHeroTitle())
                .heroDescription(setting.getHeroDescription())
                .heroImageUrl(setting.getHeroImageUrl())
                .serviceVisible(setting.isServiceVisible())
                .serviceTitle(setting.getServiceTitle())
                .styleVisible(setting.isStyleVisible())
                .styleTitle(setting.getStyleTitle())
                .styleDescription(setting.getStyleDescription())
                .build();
    }


    // 관리자에서 변경한 사이트 설정값 저장
    @Transactional
    public SiteSettingDto saveSetting(SiteSettingRequest request) {

        // 기존 설정이 있으면 수정하고,
        // 없으면 기본 설정을 기준으로 새로 생성
        SiteSetting setting = siteSettingRepository
                .findTopByOrderBySiteSettingNoAsc()
                .orElseGet(this::createDefaultSetting);


        // 메인 화면 설정
        setting.setHeroTitle(
                request.getHeroTitle()
        );

        setting.setHeroDescription(
                request.getHeroDescription()
        );


        // 메인 이미지는 다음 단계에서 실제 파일 업로드 기능 연결
        // 현재는 기존 이미지 경로 유지


        // 서비스 안내 설정
        setting.setServiceVisibleYn(
                request.isServiceVisible() ? "Y" : "N"
        );

        setting.setServiceTitle(
                request.getServiceTitle()
        );


        // 헤어스타일 영역 설정
        setting.setStyleVisibleYn(
                request.isStyleVisible() ? "Y" : "N"
        );

        setting.setStyleTitle(
                request.getStyleTitle()
        );

        setting.setStyleDescription(
                request.getStyleDescription()
        );


        // DB 저장
        SiteSetting savedSetting =
                siteSettingRepository.save(setting);


        // 저장된 값을 다시 DTO로 반환
        return toDto(savedSetting);
    }
}