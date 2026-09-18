package com.young04.lastproject.reservationimage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class ReservationImageWebConfig
        implements WebMvcConfigurer {

    private final ReservationPrivateImageInterceptor
            reservationPrivateImageInterceptor;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {
        String location =
                Path.of(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .toUri()
                        .toString();

        /*
         * 예약 참고 이미지 등 업로드 파일을
         * /uploads/** URL로 제공한다.
         *
         * 헤어스타일 예시 이미지는
         * src/main/resources/static/images/hairstyle 에서
         * 정적 리소스로 제공한다.
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {
        /*
         * 단, 고객 예약 참고 이미지만 직접 URL 접근을 막는다.
         */
        registry.addInterceptor(
                        reservationPrivateImageInterceptor
                )
                .addPathPatterns(
                        "/uploads/reservation/**"
                );
    }
}
