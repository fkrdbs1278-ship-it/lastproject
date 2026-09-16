package com.young04.lastproject.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

// 사용자 메인 Hero 업로드 이미지를 브라우저에서 조회할 수 있도록 연결
@Configuration
public class SiteAdminWebConfig implements WebMvcConfigurer {

    private static final Path SITEADMIN_UPLOAD_DIRECTORY =
            Path.of("siteadmin-upload")
                    .toAbsolutePath()
                    .normalize();


    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        registry
                .addResourceHandler(
                        "/siteadmin-upload/**"
                )
                .addResourceLocations(
                        SITEADMIN_UPLOAD_DIRECTORY
                                .toUri()
                                .toString()
                );
    }
}
