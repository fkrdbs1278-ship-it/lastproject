package com.young04.lastproject.salonholiday;

import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SalonHolidayRepositoryTest {

    @Autowired
    SalonHolidayRepository salonHolidayRepository;

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
    void 등록된_휴일이_없으면_겹치는_휴일은_0건이다() {

        LocalDateTime start =
                LocalDateTime.of(
                        2026, 8, 30,
                        14, 0
                );

        LocalDateTime end =
                LocalDateTime.of(
                        2026, 8, 30,
                        15, 0
                );

        long count =
                salonHolidayRepository
                        .countOverlappingHolidays(
                                start,
                                end
                        );

        System.out.println(
                "겹치는 휴일 수 = " + count
        );

        assertThat(count).isZero();
    }
}