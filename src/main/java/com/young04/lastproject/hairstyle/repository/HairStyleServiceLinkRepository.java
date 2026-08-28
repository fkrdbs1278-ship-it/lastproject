package com.young04.lastproject.hairstyle.repository;

import com.young04.lastproject.hairstyle.entity.HairStyleServiceLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HairStyleServiceLinkRepository
    extends JpaRepository<HairStyleServiceLink, Long> {

    /*특정 헤어 스타일과 연결된 시술 메뉴 조회
    예: 레이어드 C컽 -> 디자인 커트, C컽 펌, 클리닉
     */

    List<HairStyleServiceLink>
    findByHairStyle_NoOrderByServiceMenu_DisplayOrderAscServiceMenu_NoAsc(
            Long hairStyleNo
    );





}
