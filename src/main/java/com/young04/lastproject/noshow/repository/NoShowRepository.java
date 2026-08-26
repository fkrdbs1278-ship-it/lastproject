package com.young04.lastproject.noshow.repository;

import com.young04.lastproject.noshow.entity.NoShow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoShowRepository extends JpaRepository<NoShow, Long> {

    Optional<NoShow> findByReservationReservationNo(Long reservationNo);

    boolean existsByReservationReservationNo(Long reservationNo);
}
