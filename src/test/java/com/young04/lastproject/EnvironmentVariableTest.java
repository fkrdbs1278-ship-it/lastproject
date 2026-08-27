package com.young04.lastproject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentVariableTest {

    @Test
    void DB_환경변수_확인() {

        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");

        System.out.println("DB_URL 존재 = " + (dbUrl != null));
        System.out.println("DB_USERNAME 존재 = " + (dbUsername != null));
        System.out.println("DB_PASSWORD 존재 = " + (dbPassword != null));

        if (dbUrl != null) {
            System.out.println("DB_URL 길이 = " + dbUrl.length());
        }

        if (dbUsername != null) {
            System.out.println("DB_USERNAME 길이 = " + dbUsername.length());
        }

        if (dbPassword != null) {
            System.out.println("DB_PASSWORD 길이 = " + dbPassword.length());
        }

        assertThat(dbUrl).isNotBlank();
        assertThat(dbUsername).isNotBlank();
        assertThat(dbPassword).isNotBlank();
    }
}