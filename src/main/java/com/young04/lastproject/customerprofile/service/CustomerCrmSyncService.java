package com.young04.lastproject.customerprofile.service;

import com.young04.lastproject.customerprofile.dto.CustomerCreateRequest;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentStatus;
import com.young04.lastproject.payment.repository.PaymentRepository;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.treatmenthistory.entity.TreatmentHistory;
import com.young04.lastproject.treatmenthistory.service.TreatmentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerCrmSyncService {

    private final CustomerProfileService customerProfileService;

    private final TreatmentHistoryService treatmentHistoryService;

    private final ReservationRepository reservationRepository;

    private final PaymentRepository paymentRepository;

    private final MemberRepository memberRepository;


    @Transactional
    public void synchronizeAllCustomers() {

        List<Reservation> completedReservations =
                reservationRepository
                        .findByStatusOrderByStartAtAsc(
                                ReservationStatus.COMPLETED
                        );


        for (Reservation reservation : completedReservations) {

            Optional<CustomerProfile> customerOptional =
                    findOrCreateCustomer(
                            reservation
                    );


            if (customerOptional.isPresent()) {

                createTreatmentHistoryIfAbsent(
                        customerOptional.get(),
                        reservation
                );
            }
        }


        List<CustomerProfile> customers =
                customerProfileService.findAllCustomers();


        for (CustomerProfile customer : customers) {

            synchronizeStatistics(
                    customer
            );
        }
    }


    @Transactional
    public void synchronizeCustomer(
            Long customerId
    ) {

        CustomerProfile customer =
                customerProfileService.getCustomerById(
                        customerId
                );


        List<Reservation> completedReservations =
                findCompletedReservations(
                        customer
                );


        for (Reservation reservation : completedReservations) {

            createTreatmentHistoryIfAbsent(
                    customer,
                    reservation
            );
        }


        synchronizeStatistics(
                customer
        );
    }


    @Transactional
    public void synchronizeCompletedReservation(
            Reservation reservation
    ) {

        Optional<CustomerProfile> customerOptional =
                findOrCreateCustomer(
                        reservation
                );


        if (customerOptional.isEmpty()) {

            log.warn(
                    "CRM 시술 이력 동기화 생략 reservationNo={}",
                    reservation.getReservationNo()
            );

            return;
        }


        CustomerProfile customer =
                customerOptional.get();


        createTreatmentHistoryIfAbsent(
                customer,
                reservation
        );


        synchronizeStatistics(
                customer
        );
    }


    @Transactional
    public void synchronizePayment(
            Payment payment
    ) {

        if (payment == null
                || payment.getReservation() == null) {

            return;
        }


        Optional<CustomerProfile> customerOptional =
                findOrCreateCustomer(
                        payment.getReservation()
                );


        if (customerOptional.isEmpty()) {

            log.warn(
                    "CRM 결제 집계 동기화 생략 paymentNo={}",
                    payment.getPaymentNo()
            );

            return;
        }


        synchronizeStatistics(
                customerOptional.get()
        );
    }


    private List<Reservation> findCompletedReservations(
            CustomerProfile customer
    ) {

        Map<Long, Reservation> reservationMap =
                new LinkedHashMap<>();


        if (customer.getMemberNo() != null) {

            List<Reservation> memberReservations =
                    reservationRepository
                            .findByMemberNoAndStatusOrderByStartAtAsc(
                                    customer.getMemberNo(),
                                    ReservationStatus.COMPLETED
                            );


            for (Reservation reservation : memberReservations) {

                reservationMap.put(
                        reservation.getReservationNo(),
                        reservation
                );
            }
        }


        String phoneDigits =
                normalizePhone(
                        customer.getPhone()
                );


        if (!phoneDigits.isBlank()) {

            List<Reservation> guestReservations =
                    reservationRepository
                            .findByCustomerTypeAndGuestPhoneAndStatusOrderByStartAtAsc(
                                    CustomerType.GUEST,
                                    phoneDigits,
                                    ReservationStatus.COMPLETED
                            );


            for (Reservation reservation : guestReservations) {

                reservationMap.put(
                        reservation.getReservationNo(),
                        reservation
                );
            }
        }


        return reservationMap
                .values()
                .stream()
                .toList();
    }


    private void createTreatmentHistoryIfAbsent(
            CustomerProfile customer,
            Reservation reservation
    ) {

        boolean exists =
                treatmentHistoryService
                        .findByReservationNo(
                                reservation.getReservationNo()
                        )
                        .isPresent();


        if (exists) {

            return;
        }


        LocalDate treatmentDate =
                reservation.getCompletedAt() != null
                        ? reservation.getCompletedAt().toLocalDate()
                        : reservation.getStartAt().toLocalDate();


        long treatmentPriceValue =
                reservation.getFinalPriceSnapshot() != null
                        ? reservation.getFinalPriceSnapshot()
                        : 0L;


        treatmentHistoryService
                .createTreatmentHistory(
                        customer.getCustomerId(),
                        reservation.getReservationNo(),
                        reservation.getServiceMenuNo(),
                        reservation.getServiceNameSnapshot(),
                        treatmentDate,
                        BigDecimal.valueOf(
                                treatmentPriceValue
                        ),
                        null,
                        null
                );
    }


    private void synchronizeStatistics(
            CustomerProfile customer
    ) {

        List<TreatmentHistory> treatments =
                treatmentHistoryService
                        .findByCustomerId(
                                customer.getCustomerId()
                        );


        LocalDate lastVisitDate =
                treatments.isEmpty()
                        ? null
                        : treatments
                                .get(0)
                                .getTreatmentDate();


        int visitCount =
                treatments.size();


        long paymentAmount =
                calculatePaidAmount(
                        customer
                );


        customerProfileService
                .synchronizeStatistics(
                        customer.getCustomerId(),
                        lastVisitDate,
                        visitCount,
                        BigDecimal.valueOf(
                                paymentAmount
                        )
                );
    }


    private long calculatePaidAmount(
            CustomerProfile customer
    ) {

        long total =
                0L;


        if (customer.getMemberNo() != null) {

            Long memberPayment =
                    paymentRepository
                            .sumPaymentAmountByMemberNo(
                                    customer.getMemberNo(),
                                    PaymentStatus.PAID
                            );


            total +=
                    memberPayment == null
                            ? 0L
                            : memberPayment;
        }


        String phoneDigits =
                normalizePhone(
                        customer.getPhone()
                );


        if (!phoneDigits.isBlank()) {

            Long guestPayment =
                    paymentRepository
                            .sumPaymentAmountByGuestPhone(
                                    phoneDigits,
                                    CustomerType.GUEST,
                                    PaymentStatus.PAID
                            );


            total +=
                    guestPayment == null
                            ? 0L
                            : guestPayment;
        }


        return total;
    }


    private Optional<CustomerProfile> findOrCreateCustomer(
            Reservation reservation
    ) {

        if (reservation.getMemberNo() != null) {

            Optional<CustomerProfile> customer =
                    customerProfileService
                            .findByMemberNo(
                                    reservation.getMemberNo()
                            );


            if (customer.isPresent()) {

                return customer;
            }


            Optional<Member> member =
                    memberRepository
                            .findById(
                                    reservation.getMemberNo()
                            );


            if (member.isPresent()) {

                return Optional.of(
                        customerProfileService
                                .syncMemberCustomer(
                                        member.get()
                                )
                );
            }


            return Optional.empty();
        }


        if (reservation.getGuestPhone() == null
                || reservation.getGuestPhone().isBlank()) {

            return Optional.empty();
        }


        Optional<CustomerProfile> customer =
                customerProfileService
                        .findByPhone(
                                reservation.getGuestPhone()
                        );


        if (customer.isPresent()) {

            return customer;
        }


        CustomerCreateRequest request =
                new CustomerCreateRequest();

        request.setCustomerName(
                reservation.getGuestName()
        );

        request.setPhone(
                reservation.getGuestPhone()
        );


        return Optional.of(
                customerProfileService
                        .createGuestCustomer(
                                request
                        )
        );
    }


    private String normalizePhone(
            String phone
    ) {

        if (phone == null) {

            return "";
        }


        return phone.replaceAll(
                "[^0-9]",
                ""
        );
    }
}
