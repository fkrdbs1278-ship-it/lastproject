package com.young04.lastproject.businesshour;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BusinessHourRepositoryTest {

    @Autowired
    BusinessHourRepository businessHourRepository;

    @Test
    void 요일별_영업시간_조회() {

        BusinessHour businessHour =
                businessHourRepository.findByDayOfWeek(1)
                        .orElseThrow();

        System.out.println(
                "요일 = " + businessHour.getDayOfWeek()
        );

        System.out.println(
                "영업 여부 = " + businessHour.getIsOpen()
        );

        System.out.println(
                "오픈 시간 = " + businessHour.getOpenTime()
        );

        System.out.println(
                "마감 시간 = " + businessHour.getCloseTime()
        );

        assertThat(
                businessHour.getDayOfWeek()
        ).isEqualTo(1);
    }


//    @Nested
//    class EnvironmentVariableTest {
//        @Test
//        void DB_환경변수_확인() {
//
//            String dbUrl = System.getenv("DB_URL");
//            String dbUsername = System.getenv("DB_USERNAME");
//            String dbPassword = System.getenv("DB_PASSWORD");
//
//            System.out.println("DB_URL 존재 = " + (dbUrl != null));
//            System.out.println("DB_USERNAME 존재 = " + (dbUsername != null));
//            System.out.println("DB_PASSWORD 존재 = " + (dbPassword != null));
//
//            if (dbUrl != null) {
//                System.out.println("DB_URL 길이 = " + dbUrl.length());
//            }
//
//            if (dbUsername != null) {
//                System.out.println("DB_USERNAME 길이 = " + dbUsername.length());
//            }
//
//            if (dbPassword != null) {
//                System.out.println("DB_PASSWORD 길이 = " + dbPassword.length());
//            }
//
//            assertThat(dbUrl).isNotBlank();
//            assertThat(dbUsername).isNotBlank();
//            assertThat(dbPassword).isNotBlank();
//        }
//    }
}