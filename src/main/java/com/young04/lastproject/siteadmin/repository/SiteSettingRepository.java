package com.young04.lastproject.siteadmin.repository;

import com.young04.lastproject.siteadmin.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 사용자 메인 화면 설정값의 DB 조회와 저장을 담당하는 Repository
public interface SiteSettingRepository
        extends JpaRepository<SiteSetting, Long> {

    // 가장 먼저 등록된 사이트 설정 한 건 조회
    Optional<SiteSetting> findTopByOrderBySiteSettingNoAsc();
}