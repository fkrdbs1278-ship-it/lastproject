package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.HairStyleReader;
import com.young04.lastproject.reservation.service.ReservationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class Phase4_1IntegrationTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    HairStyleReader hairStyleReader;

    @Autowired
    AdminReservationService adminReservationService;

    @Autowired
    Validator validator;

    @PersistenceContext
    EntityManager entityManager;

    Long serviceMenuNo;
    Long hairStyleNo;

    @BeforeEach
    void setUp() {
        serviceMenuNo =
                ((Number) entityManager.createNativeQuery("""
                        SELECT NO
                        FROM SERVICE_MENU
                        WHERE NAME = 'PHASE2_TEST_CUT_30'
                        """)
                        .getSingleResult())
                        .longValue();

        hairStyleNo =
                ((Number) entityManager.createNativeQuery("""
                        SELECT NO
                        FROM HAIR_STYLE
                        WHERE TITLE = 'PHASE4_1_TEST_STYLE'
                        """)
                        .getSingleResult())
                        .longValue();
    }

    @Test
    void 시술메뉴에_연결된_헤어스타일을_조회한다() {
        assertThat(
                hairStyleReader
                        .getActiveStylesForService(serviceMenuNo)
        )
                .anySatisfy(style -> {
                    assertThat(style.getHairStyleNo())
                            .isEqualTo(hairStyleNo);

                    assertThat(style.getTitle())
                            .isEqualTo("PHASE4_1_TEST_STYLE");
                });
    }

    @Test
    void 비회원_예약에_헤어스타일을_저장한다() {
        ReservationCreateRequest request =
                validGuestRequest(
                        LocalDateTime.of(2099, 1, 5, 10, 0)
                );

        request.setHairStyleNo(hairStyleNo);
        request.setRequestMemo("예시 스타일처럼 시술해주세요.");

        ReservationResponse result =
                reservationService.createReservation(request);

        assertThat(result.getReservationNo())
                .isNotNull();

        assertThat(result.getHairStyleNo())
                .isEqualTo(hairStyleNo);

        assertThat(result.getGuestPhone())
                .isEqualTo("01056550100");
    }

    @Test
    void 비회원은_예약번호와_전화번호로_자기예약을_조회한다() {
        ReservationResponse created =
                reservationService.createReservation(
                        validGuestRequest(
                                LocalDateTime.of(2099, 1, 5, 11, 0)
                        )
                );

        GuestReservationLookupRequest lookup =
                new GuestReservationLookupRequest();

        lookup.setReservationNo(
                created.getReservationNo()
        );

        // 하이픈 입력도 서버에서 숫자로 정규화되어 조회되어야 함
        lookup.setGuestPhone(
                "010-5655-0100"
        );

        ReservationResponse found =
                reservationService.lookupGuestReservation(lookup);

        assertThat(found.getReservationNo())
                .isEqualTo(created.getReservationNo());

        assertThat(found.getGuestPhone())
                .isEqualTo("01056550100");
    }

    @Test
    void 잘못된_비회원_전화번호는_BeanValidation에서_차단한다() {
        ReservationCreateRequest request =
                validGuestRequest(
                        LocalDateTime.of(2099, 1, 5, 12, 0)
                );

        request.setGuestPhone(
                "56789+23467"
        );

        Set<ConstraintViolation<ReservationCreateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .anySatisfy(v ->
                        assertThat(
                                v.getPropertyPath().toString()
                        ).isEqualTo("guestPhone")
                );
    }

    @Test
    void 숫자와_특수문자만_있는_이름은_차단한다() {
        ReservationCreateRequest request =
                validGuestRequest(
                        LocalDateTime.of(2099, 1, 5, 13, 0)
                );

        request.setGuestName(
                "123@@@"
        );

        Set<ConstraintViolation<ReservationCreateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .anySatisfy(v ->
                        assertThat(
                                v.getPropertyPath().toString()
                        ).isEqualTo("guestName")
                );
    }

    @Test
    void 요청사항은_500자를_초과할수없다() {
        ReservationCreateRequest request =
                validGuestRequest(
                        LocalDateTime.of(2099, 1, 5, 14, 0)
                );

        request.setRequestMemo(
                "가".repeat(501)
        );

        Set<ConstraintViolation<ReservationCreateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .anySatisfy(v ->
                        assertThat(
                                v.getPropertyPath().toString()
                        ).isEqualTo("requestMemo")
                );
    }

    @Test
    void 관리자_상세에서_요청사항과_헤어스타일을_확인한다() {
        ReservationCreateRequest request =
                validGuestRequest(
                        LocalDateTime.of(2099, 1, 5, 15, 0)
                );

        request.setHairStyleNo(hairStyleNo);
        request.setRequestMemo(
                "앞머리는 조금만 다듬어주세요."
        );

        ReservationResponse created =
                reservationService.createReservation(request);

        AdminReservationDetailResponse detail =
                adminReservationService.detail(
                        created.getReservationNo()
                );

        assertThat(
                detail.getReservation()
                        .getRequestMemo()
        ).isEqualTo(
                "앞머리는 조금만 다듬어주세요."
        );

        assertThat(detail.getHairStyleTitle())
                .isEqualTo(
                        "PHASE4_1_TEST_STYLE"
                );
    }

    private ReservationCreateRequest validGuestRequest(
            LocalDateTime startAt
    ) {
        ReservationCreateRequest request =
                new ReservationCreateRequest();

        request.setGuestName("김영준");
        request.setGuestPhone("010-5655-0100");
        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(startAt);
        request.setReservationSource(
                ReservationSource.ONLINE
        );

        return request;
    }
}
