package com.young04.lastproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        // @Future 및 기존 LocalDateTime.now()와 같은 JVM 시간대를 사용한다.
        // 한국 매장 운영 서버: -Duser.timezone=Asia/Seoul
        return Clock.systemDefaultZone();
    }
}
