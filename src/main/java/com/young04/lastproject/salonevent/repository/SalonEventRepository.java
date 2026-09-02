package com.young04.lastproject.salonevent.repository;

import com.young04.lastproject.salonevent.entity.SalonEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// 이벤트 데이터의 저장과 조회를 담당하는 Repository
public interface SalonEventRepository
        extends JpaRepository<SalonEvent, Long> {

    // 관리자용 전체 이벤트 최신 등록순 조회
    List<SalonEvent> findAllByOrderByRegdateDesc();

    // 사용 여부별 이벤트 최신 등록순 조회
    List<SalonEvent> findByUseYnOrderByRegdateDesc(String useYn);

    // 이벤트 제목 검색
    List<SalonEvent> findByEventTitleContainingIgnoreCaseOrderByRegdateDesc(
            String eventTitle
    );

    // 사용 여부와 이벤트 제목으로 검색
    List<SalonEvent>
    findByUseYnAndEventTitleContainingIgnoreCaseOrderByRegdateDesc(
            String useYn,
            String eventTitle
    );

    // 현재 사용자에게 노출할 진행 중 이벤트 조회
    List<SalonEvent>
    findByUseYnAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
            String useYn,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 대시보드용 진행 중 이벤트를 종료일이 가까운 순서로 최대 2개 조회
    List<SalonEvent>
    findTop2ByUseYnAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(
            String useYn,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
