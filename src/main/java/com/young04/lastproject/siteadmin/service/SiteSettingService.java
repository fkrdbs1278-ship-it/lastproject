package com.young04.lastproject.siteadmin.service;

import com.young04.lastproject.siteadmin.dto.SiteSettingDto;
import com.young04.lastproject.siteadmin.dto.SiteSettingRequest;
import com.young04.lastproject.siteadmin.entity.SiteSetting;
import com.young04.lastproject.siteadmin.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

// 관리자 설정값을 조회하고 사용자 메인 화면에 전달하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteSettingService {

    private static final long MAX_HERO_IMAGE_SIZE =
            10L * 1024L * 1024L;

    private static final String HERO_UPLOAD_URL_PREFIX =
            "/siteadmin-upload/";

    private static final Path HERO_UPLOAD_DIRECTORY =
            Path.of("siteadmin-upload")
                    .toAbsolutePath()
                    .normalize();

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
            );


    private final SiteSettingRepository siteSettingRepository;

    private static final String DEFAULT_HERO_IMAGE_URL =
            "/images/hero/hero1.jpg";


    @Transactional
    public void resetHeroImage() {

        SiteSetting setting =
                siteSettingRepository
                        .findTopByOrderBySiteSettingNoAsc()
                        .orElseGet(
                                this::createDefaultSetting
                        );


        // 현재 관리자 업로드 이미지 주소
        String previousHeroImageUrl =
                setting.getHeroImageUrl();


        // 기본 이미지로 변경
        setting.setHeroImageUrl(
                DEFAULT_HERO_IMAGE_URL
        );


        // DB 저장
        siteSettingRepository.save(
                setting
        );


        /*
         * 기존 이미지가 /siteadmin-upload/에
         * 업로드된 파일이었다면 삭제
         *
         * hero1.jpg 같은 static 기본 이미지는
         * deletePreviousUploadedHeroImage()에서
         * 삭제하지 않음
         */
        deletePreviousUploadedHeroImage(
                previousHeroImageUrl,
                DEFAULT_HERO_IMAGE_URL
        );
    }



    // 현재 사용자 사이트 설정 조회
    public SiteSettingDto getCurrentSetting() {

        SiteSetting setting = siteSettingRepository
                .findTopByOrderBySiteSettingNoAsc()
                .orElseGet(this::createDefaultSetting);

        return toDto(setting);
    }


    // DB에 설정이 없을 때 사용할 기본값 생성
    private SiteSetting createDefaultSetting() {

        return SiteSetting.builder()
                .heroTitle(
                        "나에게 어울리는 스타일을 찾아보세요."
                )
                .heroDescription(
                        "원하는 시술과 헤어스타일을 확인하고 편리하게 예약 서비스를 이용해보세요."
                )
                .heroImageUrl(
                        "/images/hero/hero1.jpg"
                )
                .serviceVisibleYn("Y")
                .serviceTitle("서비스 안내")
                .styleVisibleYn("Y")
                .styleTitle("헤어스타일 둘러보기")
                .styleDescription(
                        "다양한 스타일을 확인하고 원하는 헤어스타일을 찾아보세요."
                )
                .build();
    }


    // Entity 데이터를 화면에서 사용할 DTO로 변환
    private SiteSettingDto toDto(
            SiteSetting setting
    ) {

        return SiteSettingDto.builder()
                .heroTitle(
                        setting.getHeroTitle()
                )
                .heroDescription(
                        setting.getHeroDescription()
                )
                .heroImageUrl(
                        setting.getHeroImageUrl()
                )
                .serviceVisible(
                        setting.isServiceVisible()
                )
                .serviceTitle(
                        setting.getServiceTitle()
                )
                .styleVisible(
                        setting.isStyleVisible()
                )
                .styleTitle(
                        setting.getStyleTitle()
                )
                .styleDescription(
                        setting.getStyleDescription()
                )
                .build();
    }


    // 관리자에서 변경한 사이트 설정값 저장
    @Transactional
    public SiteSettingDto saveSetting(
            SiteSettingRequest request
    ) {

        // 기존 설정이 있으면 수정하고,
        // 없으면 기본 설정을 기준으로 새로 생성
        SiteSetting setting =
                siteSettingRepository
                        .findTopByOrderBySiteSettingNoAsc()
                        .orElseGet(
                                this::createDefaultSetting
                        );


        // 메인 화면 문구
        setting.setHeroTitle(
                request.getHeroTitle()
        );

        setting.setHeroDescription(
                request.getHeroDescription()
        );


        /*
         * 관리자 화면의 메인 이미지 업로드는
         * 사용자 Hero 4장 중 1번 슬라이드만 교체합니다.
         *
         * 2~4번은 기존 static 이미지
         * hero2.jpg / hero3.jpg / hero4.jpg 를 그대로 사용합니다.
         */
        MultipartFile heroImage =
                request.getHeroImage();

        String previousHeroImageUrl =
                setting.getHeroImageUrl();

        String newHeroImageUrl = null;


        if (
                heroImage != null
                        && !heroImage.isEmpty()
        ) {

            newHeroImageUrl =
                    saveHeroImage(heroImage);

            setting.setHeroImageUrl(
                    newHeroImageUrl
            );
        }


        /*
         * 서비스 안내 UI는 관리자 화면에서 제거했지만
         * 현재 DB / Entity 구조 호환을 위해 값은 유지합니다.
         */
        setting.setServiceVisibleYn(
                request.isServiceVisible()
                        ? "Y"
                        : "N"
        );

        setting.setServiceTitle(
                request.getServiceTitle()
        );


        // 헤어스타일 영역 설정
        setting.setStyleVisibleYn(
                request.isStyleVisible()
                        ? "Y"
                        : "N"
        );

        setting.setStyleTitle(
                request.getStyleTitle()
        );

        setting.setStyleDescription(
                request.getStyleDescription()
        );


        // DB 저장
        SiteSetting savedSetting =
                siteSettingRepository.save(setting);


        /*
         * 새 이미지와 DB 저장까지 성공한 뒤
         * 예전에 업로드했던 1번 이미지만 정리합니다.
         *
         * /images/hero/hero1.jpg 같은 기본 static 이미지는 삭제하지 않습니다.
         */
        if (newHeroImageUrl != null) {

            deletePreviousUploadedHeroImage(
                    previousHeroImageUrl,
                    newHeroImageUrl
            );
        }


        return toDto(savedSetting);
    }


    // 메인 Hero 1번 이미지 파일 저장
    private String saveHeroImage(
            MultipartFile heroImage
    ) {

        validateHeroImage(heroImage);


        String extension =
                resolveExtension(
                        heroImage.getOriginalFilename()
                );


        String savedFileName =
                "hero-main-"
                        + UUID.randomUUID()
                        + "."
                        + extension;


        try {

            Files.createDirectories(
                    HERO_UPLOAD_DIRECTORY
            );


            Path savePath =
                    HERO_UPLOAD_DIRECTORY
                            .resolve(savedFileName)
                            .normalize();


            if (
                    !savePath.startsWith(
                            HERO_UPLOAD_DIRECTORY
                    )
            ) {

                throw new IllegalArgumentException(
                        "올바르지 않은 이미지 파일명입니다."
                );
            }


            Files.copy(
                    heroImage.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return HERO_UPLOAD_URL_PREFIX
                    + savedFileName;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "메인 이미지 저장 중 오류가 발생했습니다.",
                    exception
            );
        }
    }


    // 업로드 파일 검증
    private void validateHeroImage(
            MultipartFile heroImage
    ) {

        if (heroImage.getSize() > MAX_HERO_IMAGE_SIZE) {

            throw new IllegalArgumentException(
                    "메인 이미지는 10MB 이하만 업로드할 수 있습니다."
            );
        }


        String contentType =
                heroImage.getContentType();


        if (
                contentType == null
                        || !contentType.startsWith("image/")
        ) {

            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }


        String extension =
                resolveExtension(
                        heroImage.getOriginalFilename()
                );


        if (
                !ALLOWED_IMAGE_EXTENSIONS.contains(
                        extension
                )
        ) {

            throw new IllegalArgumentException(
                    "JPG, JPEG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }
    }


    // 확장자 추출
    private String resolveExtension(
            String originalFileName
    ) {

        if (
                originalFileName == null
                        || originalFileName.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "이미지 파일명을 확인할 수 없습니다."
            );
        }


        int dotIndex =
                originalFileName
                        .lastIndexOf('.');


        if (
                dotIndex < 0
                        || dotIndex
                        == originalFileName.length() - 1
        ) {

            throw new IllegalArgumentException(
                    "이미지 파일 확장자를 확인할 수 없습니다."
            );
        }


        return originalFileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }


    // 이전에 업로드했던 관리자 Hero 파일 정리
    private void deletePreviousUploadedHeroImage(
            String previousUrl,
            String newUrl
    ) {

        if (
                previousUrl == null
                        || previousUrl.isBlank()
                        || previousUrl.equals(newUrl)
                        || !previousUrl.startsWith(
                        HERO_UPLOAD_URL_PREFIX
                )
        ) {

            return;
        }


        String previousFileName =
                previousUrl.substring(
                        HERO_UPLOAD_URL_PREFIX.length()
                );


        try {

            Path previousPath =
                    HERO_UPLOAD_DIRECTORY
                            .resolve(previousFileName)
                            .normalize();


            if (
                    previousPath.startsWith(
                            HERO_UPLOAD_DIRECTORY
                    )
            ) {

                Files.deleteIfExists(
                        previousPath
                );
            }

        } catch (IOException ignored) {

            /*
             * 이전 이미지 정리 실패는
             * 새 설정 저장 자체를 실패시키지 않습니다.
             */
        }
    }
}