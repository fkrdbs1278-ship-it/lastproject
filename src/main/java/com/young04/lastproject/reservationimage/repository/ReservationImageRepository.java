package com.young04.lastproject.reservationimage.repository;

import com.young04.lastproject.reservationimage.entity.ReservationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationImageRepository
        extends JpaRepository<ReservationImage, Long> {

    List<ReservationImage> findByReservationReservationNoOrderBySortOrderAsc(
            Long reservationNo
    );

    void deleteByReservationReservationNo(Long reservationNo);
}
