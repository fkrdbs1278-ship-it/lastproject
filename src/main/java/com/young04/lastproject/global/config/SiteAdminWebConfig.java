package com.young04.lastproject.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

// 사용자 메인 Hero 업로드 이미지를 브라우저에서 조회할 수 있도록 연결
@Configuration
public class SiteAdminWebConfig implements WebMvcConfigurer {

    private final Path siteAdminUploadDirectory;

    public SiteAdminWebConfig(
            @Value("${file.siteadmin-upload-dir:siteadmin-upload}")
            String siteAdminUploadDir
    ) {
        this.siteAdminUploadDirectory =
                Path.of(siteAdminUploadDir)
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {
        registry
                .addResourceHandler(
                        "/siteadmin-upload/**"
                )
                .addResourceLocations(
                        siteAdminUploadDirectory
                                .toUri()
                                .toString()
                );
    }
}
