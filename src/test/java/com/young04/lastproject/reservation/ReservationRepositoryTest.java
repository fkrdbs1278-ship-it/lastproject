package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                () -> System.getenv("DB_URL")
        );

        registry.add(
                "spring.datasource.username",
                () -> System.getenv("DB_USERNAME")
        );

        registry.add(
                "spring.datasource.password",
                () -> System.getenv("DB_PASSWORD")
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "oracle.jdbc.OracleDriver"
        );
    }

    @Test
    void 회원별_예약내역_조회() {

        Long memberNo = 999999L;

        List<Reservation> result =
                reservationRepository
                        .findByMemberNoOrderByStartAtDesc(memberNo);

        System.out.println(
                "조회된 예약 수 = " + result.size()
        );

        assertThat(result).isNotNull();
    }

    @Test
    void 회원예약_저장() {

        // given
        Reservation reservation =
                Reservation.createMemberReservation(
                        1L,
                        1L,
                        "테스트 커트",
                        30,
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 0
                        ),
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 30
                        ),
                        "예약 테스트입니다.",
                        ReservationSource.ONLINE
                );

        // when
        Reservation saved =
                reservationRepository.saveAndFlush(reservation);

        // then
        System.out.println(
                "예약번호 = "
                        + saved.getReservationNo()
        );

        System.out.println(
                "예약상태 = "
                        + saved.getStatus()
        );

        System.out.println(
                "예약시간 = "
                        + saved.getStartAt()
                        + " ~ "
                        + saved.getEndAt()
        );

        assertThat(
                saved.getReservationNo()
        ).isNotNull();

        assertThat(
                saved.getStatus()
        ).isEqualTo(
                ReservationStatus.REQUESTED
        );
    }

    @Test
    void 예약시간이_겹치면_중복예약으로_판단한다() {

        // given
        Reservation existing =
                Reservation.createMemberReservation(
                        1L,   // 실제 테스트 회원 NO
                        1L,   // 실제 테스트 메뉴 NO
                        "테스트 커트",
                        30,
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 0
                        ),
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 30
                        ),
                        null,
                        ReservationSource.ONLINE
                );

        reservationRepository.saveAndFlush(existing);

        // 신규 요청: 14:10 ~ 14:40
        LocalDateTime requestedStart =
                LocalDateTime.of(
                        2026, 8, 28,
                        14, 10
                );

        LocalDateTime requestedEnd =
                LocalDateTime.of(
                        2026, 8, 28,
                        14, 40
                );

        // when
        long count =
                reservationRepository
                        .countOverlappingReservations(
                                requestedStart,
                                requestedEnd,
                                List.of(
                                        ReservationStatus.REQUESTED,
                                        ReservationStatus.CONFIRMED
                                )
                        );

        // then
        System.out.println(
                "겹치는 예약 수 = " + count
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void 기존예약_종료시간과_새예약_시작시간이_같으면_중복이아니다() {

        // given
        Reservation existing =
                Reservation.createMemberReservation(
                        1L,
                        1L,
                        "테스트 커트",
                        30,
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 0
                        ),
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 30
                        ),
                        null,
                        ReservationSource.ONLINE
                );

        reservationRepository.saveAndFlush(existing);

        // 신규 요청: 14:30 ~ 15:00
        LocalDateTime requestedStart =
                LocalDateTime.of(
                        2026, 8, 28,
                        14, 30
                );

        LocalDateTime requestedEnd =
                LocalDateTime.of(
                        2026, 8, 28,
                        15, 0
                );

        // when
        long count =
                reservationRepository
                        .countOverlappingReservations(
                                requestedStart,
                                requestedEnd,
                                List.of(
                                        ReservationStatus.REQUESTED,
                                        ReservationStatus.CONFIRMED
                                )
                        );

        // then
        System.out.println(
                "경계시간 중복 수 = " + count
        );

        assertThat(count).isZero();
    }

    @Test
    void 예약변경시_자기자신은_중복검사에서_제외한다() {

        // given
        Reservation existing =
                Reservation.createMemberReservation(
                        1L,
                        1L,
                        "테스트 커트",
                        30,
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 0
                        ),
                        LocalDateTime.of(
                                2026, 8, 28,
                                14, 30
                        ),
                        null,
                        ReservationSource.ONLINE
                );

        reservationRepository.saveAndFlush(existing);

        // when
        long count =
                reservationRepository
                        .countOverlappingReservationsExcludingSelf(
                                existing.getReservationNo(),
                                LocalDateTime.of(
                                        2026, 8, 28,
                                        14, 0
                                ),
                                LocalDateTime.of(
                                        2026, 8, 28,
                                        14, 30
                                ),
                                List.of(
                                        ReservationStatus.REQUESTED,
                                        ReservationStatus.CONFIRMED
                                )
                        );

        // then
        System.out.println(
                "자기 자신 제외 후 중복 수 = " + count
        );

        assertThat(count).isZero();
    }

    @Test
    void QueryDSL_예약상태로_검색한다() {

        // given
        Reservation reservation =
                Reservation.createMemberReservation(
                        1L,   // 실제 테스트 회원 NO
                        1L,   // 실제 테스트 메뉴 NO
                        "테스트 커트",
                        30,
                        LocalDateTime.of(
                                2026, 8, 28,
                                16, 0
                        ),
                        LocalDateTime.of(
                                2026, 8, 28,
                                16, 30
                        ),
                        null,
                        ReservationSource.ONLINE
                );

        reservationRepository.saveAndFlush(reservation);

        ReservationSearchCondition condition =
                new ReservationSearchCondition();

        condition.setStatus(
                ReservationStatus.REQUESTED
        );

        Pageable pageable =
                PageRequest.of(0, 10);

        // when
        Page<Reservation> result =
                reservationRepository.search(
                        condition,
                        pageable
                );

        // then
        System.out.println(
                "QueryDSL 검색 결과 수 = "
                        + result.getTotalElements()
        );

        result.getContent().forEach(r ->
                System.out.println(
                        "예약번호 = "
                                + r.getReservationNo()
                                + ", 상태 = "
                                + r.getStatus()
                                + ", 시작시간 = "
                                + r.getStartAt()
                )
        );

        assertThat(result.getContent())
                .isNotEmpty();

        assertThat(
                result.getContent()
                        .stream()
                        .allMatch(r ->
                                r.getStatus()
                                        == ReservationStatus.REQUESTED
                        )
        ).isTrue();
    }


}