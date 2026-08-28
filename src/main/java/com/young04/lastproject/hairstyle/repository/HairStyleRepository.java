package com.young04.lastproject.hairstyle.repository;

import com.young04.lastproject.hairstyle.entity.HairStyle;
import com.young04.lastproject.hairstyle.entity.HairStyleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HairStyleRepository
    extends JpaRepository<HairStyle, Long> {

    /* 사용자용 전체 헤어스타일 ACTIVE_YN = Y만 조회
    DISPLAY_ORDER -> NO 순서
     */
    List<HairStyle>
    findByActiveYnOrderByDisplayOrderAscNoAsc(
      String activeYn
    );

    /* 카테고리별 조회 */
    List<HairStyle>
    findByCategoryAndActiveYnOrderByDisplayOrderAscNoAsc(
            HairStyleCategory category,
            String activeYn
    );


    /*사용자용 상세 조회
     비활성화 헤어스타일은 사용자 조회불가 */

    Optional<HairStyle>
    findByNoAndActiveYn(
            Long no,
            String activeYn
    );



}
