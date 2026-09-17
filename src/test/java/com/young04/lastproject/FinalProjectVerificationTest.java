package com.young04.lastproject;

import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import com.young04.lastproject.hairstyle.service.HairStyleService;
import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.payment.service.PaymentService;
import com.young04.lastproject.purchaseorder.service.PurchaseOrderService;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.ReservationService;
import com.young04.lastproject.review.service.ReviewService;
import com.young04.lastproject.salonevent.service.SalonEventService;
import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import com.young04.lastproject.servicemenu.repository.ServiceMenuRepository;
import com.young04.lastproject.siteadmin.service.SiteSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FinalProjectVerificationTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ServiceMenuRepository serviceMenuRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void applicationContextAndCoreServicesLoad() {
        assertThat(applicationContext.getBean(ReservationService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(AdminReservationService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(PaymentService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(CustomerProfileService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(HairStyleService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(MaterialService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(PurchaseOrderService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(SalonEventService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(ReviewService.class))
                .isNotNull();

        assertThat(applicationContext.getBean(SiteSettingService.class))
                .isNotNull();
    }

    @Test
    void currentDomainEnumsMatchFinalPolicy() {
        assertThat(ServiceMenuCategory.values())
                .containsExactly(
                        ServiceMenuCategory.CUT,
                        ServiceMenuCategory.PERM,
                        ServiceMenuCategory.COLOR,
                        ServiceMenuCategory.CLINIC,
                        ServiceMenuCategory.ETC
                );

        assertThat(ReservationStatus.values())
                .containsExactly(
                        ReservationStatus.REQUESTED,
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.COMPLETED,
                        ReservationStatus.CANCELED,
                        ReservationStatus.NO_SHOW
                );
    }

    @Test
    void reservationEntityStateTransitionsAndPriceSnapshotsWork() {
        LocalDateTime startAt =
                LocalDateTime.of(
                        2030,
                        1,
                        15,
                        14,
                        0
                );

        Reservation reservation =
                Reservation.createGuestReservation(
                        "Final Test Guest",
                        "01099998888",
                        1L,
                        null,
                        "Final Test Cut",
                        30,
                        startAt,
                        startAt.plusMinutes(30),
                        "final verification",
                        ReservationSource.ONLINE
                );

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED);

        reservation.applyPricing(
                30000,
                100L,
                "Final Test Event",
                5000,
                25000
        );

        assertThat(reservation.getOriginalPriceSnapshot())
                .isEqualTo(30000);

        assertThat(reservation.getEventNoSnapshot())
                .isEqualTo(100L);

        assertThat(reservation.getDiscountAmountSnapshot())
                .isEqualTo(5000);

        assertThat(reservation.getFinalPriceSnapshot())
                .isEqualTo(25000);

        reservation.confirm();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);

        assertThat(reservation.getConfirmedAt())
                .isNotNull();

        reservation.complete();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);

        assertThat(reservation.getCompletedAt())
                .isNotNull();

        Reservation canceledReservation =
                Reservation.createGuestReservation(
                        "Cancel Test Guest",
                        "01099997777",
                        1L,
                        null,
                        "Final Test Cut",
                        30,
                        startAt.plusHours(1),
                        startAt.plusHours(1).plusMinutes(30),
                        null,
                        ReservationSource.ONLINE
                );

        canceledReservation.cancel(
                "final cancel test",
                CanceledBy.USER
        );

        assertThat(canceledReservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);

        assertThat(canceledReservation.getCancelReason())
                .isEqualTo("final cancel test");

        assertThat(canceledReservation.getCanceledAt())
                .isNotNull();
    }

    @Test
    void reservationRepositoryAndQuerydslWorkWithRollback() {
        String suffix =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 10);

        String menuName =
                "FINAL_TEST_MENU_" + suffix;

        String guestName =
                "FINAL_GUEST_" + suffix;

        ServiceMenu menu =
                ServiceMenu.create(
                        ServiceMenuCategory.CUT,
                        menuName,
                        "final verification only",
                        30000L,
                        30,
                        null,
                        "Y",
                        9999
                );

        ServiceMenu savedMenu =
                serviceMenuRepository.saveAndFlush(menu);

        LocalDateTime startAt =
                LocalDateTime.of(
                        2199,
                        11,
                        17,
                        3,
                        10
                );

        LocalDateTime endAt =
                startAt.plusMinutes(30);

        List<ReservationStatus> activeStatuses =
                List.of(
                        ReservationStatus.REQUESTED,
                        ReservationStatus.CONFIRMED
                );

        long overlapBefore =
                reservationRepository.countOverlappingReservations(
                        startAt.plusMinutes(5),
                        endAt.minusMinutes(5),
                        activeStatuses
                );

        Reservation reservation =
                Reservation.createGuestReservation(
                        guestName,
                        "01099996666",
                        savedMenu.getNo(),
                        null,
                        savedMenu.getName(),
                        savedMenu.getDurationMin(),
                        startAt,
                        endAt,
                        "final repository verification",
                        ReservationSource.ONLINE
                );

        reservation.applyPricing(
                savedMenu.getPrice().intValue(),
                null,
                null,
                0,
                savedMenu.getPrice().intValue()
        );

        Reservation savedReservation =
                reservationRepository.saveAndFlush(reservation);

        long overlapAfter =
                reservationRepository.countOverlappingReservations(
                        startAt.plusMinutes(5),
                        endAt.minusMinutes(5),
                        activeStatuses
                );

        assertThat(overlapAfter)
                .isEqualTo(overlapBefore + 1);

        ReservationSearchCondition condition =
                new ReservationSearchCondition();

        condition.setCustomerType(CustomerType.GUEST);
        condition.setGuestName(guestName);

        Page<Reservation> searchResult =
                reservationRepository.search(
                        condition,
                        PageRequest.of(0, 10)
                );

        assertThat(searchResult.getContent())
                .extracting(Reservation::getReservationNo)
                .contains(savedReservation.getReservationNo());
    }

    @Test
    void awsSecurityAndProductionConfigRemainSafe() throws IOException {
        String securityConfig =
                readNormalized(
                        "src/main/java/com/young04/lastproject/global/security/SecurityConfig.java"
                );

        assertThat(securityConfig)
                .containsPattern(
                        "\\.anyRequest\\(\\)\\s*\\.authenticated\\(\\)"
                )
                .doesNotContainPattern(
                        "\\.anyRequest\\(\\)\\s*\\.permitAll\\(\\)"
                )
                .contains("/actuator/health")
                .contains("/member/payments")
                .contains("/testcompany/**");

        String prodConfig =
                readNormalized(
                        "src/main/resources/application-prod.yaml"
                );

        assertThat(prodConfig)
                .contains("forward-headers-strategy: framework")
                .contains("shutdown: graceful")
                .contains("secure: true")
                .contains("include: health")
                .contains("show-details: never")
                .contains("maximum-pool-size:");

        String appConfig =
                readNormalized(
                        "src/main/resources/application.yaml"
                );

        assertThat(appConfig)
                .contains("url: ${DB_URL}")
                .contains("username: ${DB_USERNAME}")
                .contains("password: ${DB_PASSWORD}")
                .contains(
                        "siteadmin-upload-dir: ${SITEADMIN_UPLOAD_DIR:siteadmin-upload}"
                );

        String testCompanyController =
                readNormalized(
                        "src/main/java/com/young04/lastproject/testcompany/controller/TestCompanyController.java"
                );

        String customerTestController =
                readNormalized(
                        "src/main/java/com/young04/lastproject/treatmenthistory/controller/CustomerUserTestController.java"
                );

        assertThat(testCompanyController)
                .contains("@Profile(\"!prod\")");

        assertThat(customerTestController)
                .contains("@Profile(\"!prod\")");
    }

    @Test
    void professorRequiredArchitectureAndMainViewsExist() {
        assertFileExists(
                "src/main/java/com/young04/lastproject/reservation/exception/ReservationExceptionHandler.java"
        );

        assertFileExists(
                "src/main/java/com/young04/lastproject/member/exception/MemberExceptionAdvice.java"
        );

        assertFileExists(
                "src/main/java/com/young04/lastproject/reservation/repository/ReservationRepositoryImpl.java"
        );

        assertFileExists(
                "src/main/java/com/young04/lastproject/global/security/SecurityConfig.java"
        );

        assertFileExists(
                "src/main/java/com/young04/lastproject/loginhistory/service/LoginHistoryService.java"
        );

        assertFileExists(
                "src/main/resources/templates/index.html"
        );

        assertFileExists(
                "src/main/resources/templates/reservation/reservation-form.html"
        );

        assertFileExists(
                "src/main/resources/templates/admin/dashboard.html"
        );

        assertFileExists(
                "src/main/resources/templates/hairstyle/list.html"
        );

        assertFileExists(
                "src/main/resources/templates/review/list.html"
        );
    }

    private String readNormalized(
            String relativePath
    ) throws IOException {
        return Files.readString(
                        Path.of(relativePath)
                )
                .replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    private void assertFileExists(
            String relativePath
    ) {
        assertThat(
                Files.exists(
                        Path.of(relativePath)
                )
        )
                .as(relativePath)
                .isTrue();
    }
}
