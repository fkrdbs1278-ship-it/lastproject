package com.young04.lastproject.reservationimage.service;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.exception.ReservationNotFoundException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.entity.ReservationImage;
import com.young04.lastproject.reservationimage.exception.ReservationImageAccessDeniedException;
import com.young04.lastproject.reservationimage.exception.ReservationImageException;
import com.young04.lastproject.reservationimage.exception.ReservationImageNotFoundException;
import com.young04.lastproject.reservationimage.repository.ReservationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationImageService {

    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;

    private static final int MAX_IMAGE_COUNT = 3;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    private final ReservationRepository reservationRepository;
    private final ReservationImageRepository reservationImageRepository;
    private final ReservationMemberReader reservationMemberReader;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /*
     * =========================================================
     * 기존 Phase3 테스트 호환용
     * =========================================================
     *
     * 운영 Controller에서는 사용하지 않는다.
     * 기존 통합 테스트가 컴파일되도록 유지하는 내부 호환 메서드다.
     */
    @Transactional
    public ReservationImageResponse upload(
            Long reservationNo,
            MultipartFile file
    ) {

        ReservationImage image =
                uploadInternal(
                        reservationNo,
                        file
                );

        return ReservationImageResponse.from(image);
    }


    /*
     * =========================================================
     * 기존 Phase3 테스트 호환용
     * =========================================================
     */
    public List<ReservationImageResponse> getImages(
            Long reservationNo
    ) {

        requireReservation(reservationNo);

        return reservationImageRepository
                .findByReservationReservationNoOrderBySortOrderAsc(
                        reservationNo
                )
                .stream()
                .map(ReservationImageResponse::from)
                .toList();
    }

    @Transactional
    public ReservationImageResponse uploadForMember(
            String memberId,
            Long reservationNo,
            MultipartFile file
    ) {
        requireMemberReservation(
                memberId,
                reservationNo
        );

        ReservationImage image =
                uploadInternal(
                        reservationNo,
                        file
                );

        return ReservationImageResponse.forMember(image);
    }

    @Transactional
    public ReservationImageResponse uploadForGuest(
            Long reservationNo,
            String guestPhone,
            MultipartFile file
    ) {
        requireGuestReservation(
                reservationNo,
                guestPhone
        );

        /*
         * 응답의 fileUrl은 비회원 화면에서 직접 노출하지 않는다.
         * 업로드 성공 여부와 메타데이터 확인 용도.
         */
        return ReservationImageResponse.from(
                uploadInternal(
                        reservationNo,
                        file
                ),
                null
        );
    }

    public List<ReservationImageResponse> getMemberImages(
            String memberId,
            Long reservationNo
    ) {
        requireMemberReservation(
                memberId,
                reservationNo
        );

        return reservationImageRepository
                .findByReservationReservationNoOrderBySortOrderAsc(
                        reservationNo
                )
                .stream()
                .map(
                        ReservationImageResponse::forMember
                )
                .toList();
    }

    public List<ReservationImageResponse> getAdminImages(
            Long reservationNo
    ) {
        requireReservation(reservationNo);

        return reservationImageRepository
                .findByReservationReservationNoOrderBySortOrderAsc(
                        reservationNo
                )
                .stream()
                .map(
                        ReservationImageResponse::forAdmin
                )
                .toList();
    }

    public ReservationImageContent getMemberContent(
            String memberId,
            Long reservationNo,
            Long reservationImageNo
    ) {
        requireMemberReservation(
                memberId,
                reservationNo
        );

        return loadContent(
                reservationNo,
                reservationImageNo
        );
    }

    public ReservationImageContent getAdminContent(
            Long reservationNo,
            Long reservationImageNo
    ) {
        requireReservation(reservationNo);

        return loadContent(
                reservationNo,
                reservationImageNo
        );
    }

    @Transactional
    public void deleteForMember(
            String memberId,
            Long reservationNo,
            Long reservationImageNo
    ) {
        requireMemberReservation(
                memberId,
                reservationNo
        );

        deleteInternal(
                reservationNo,
                reservationImageNo
        );
    }

    @Transactional
    public void deleteForGuest(
            Long reservationNo,
            String guestPhone,
            Long reservationImageNo
    ) {
        requireGuestReservation(
                reservationNo,
                guestPhone
        );

        deleteInternal(
                reservationNo,
                reservationImageNo
        );
    }

    @Transactional
    public void deleteForAdmin(
            Long reservationNo,
            Long reservationImageNo
    ) {
        requireReservation(reservationNo);

        deleteInternal(
                reservationNo,
                reservationImageNo
        );
    }

    private ReservationImage uploadInternal(
            Long reservationNo,
            MultipartFile file
    ) {
        validateFile(file);

        Reservation reservation =
                requireReservation(reservationNo);

        long currentCount =
                reservationImageRepository
                        .countByReservationReservationNo(
                                reservationNo
                        );

        if (currentCount >= MAX_IMAGE_COUNT) {
            throw new ReservationImageException(
                    "예약 사진은 최대 3장까지 등록할 수 있습니다."
            );
        }

        String originalFileName =
                safeOriginalFileName(
                        file.getOriginalFilename()
                );

        String extension =
                getExtension(originalFileName);

        String storedFileName =
                UUID.randomUUID()
                        + extension;

        Path reservationDirectory =
                reservationDirectory(reservationNo);

        Path target =
                reservationDirectory
                        .resolve(storedFileName)
                        .normalize();

        if (!target.startsWith(
                reservationDirectory
        )) {
            throw new ReservationImageException(
                    "잘못된 파일 경로입니다."
            );
        }

        try {
            Files.createDirectories(
                    reservationDirectory
            );

            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new ReservationImageException(
                    "예약 사진 저장에 실패했습니다.",
                    e
            );
        }

        /*
         * DB에는 기존 구조 호환을 위해 저장 위치를 남긴다.
         * 그러나 이 URL은 WebConfig interceptor에 의해 직접 공개되지 않는다.
         */
        String fileUrl =
                "/uploads/reservation/"
                        + reservationNo
                        + "/"
                        + storedFileName;

        ReservationImage image =
                ReservationImage.create(
                        reservation,
                        originalFileName,
                        storedFileName,
                        fileUrl,
                        file.getContentType(),
                        file.getSize(),
                        (int) currentCount
                );

        try {
            return reservationImageRepository
                    .save(image);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    private ReservationImageContent loadContent(
            Long reservationNo,
            Long reservationImageNo
    ) {
        ReservationImage image =
                requireImage(
                        reservationNo,
                        reservationImageNo
                );

        Path filePath =
                reservationDirectory(reservationNo)
                        .resolve(
                                image.getStoredFileName()
                        )
                        .normalize();

        if (!filePath.startsWith(
                reservationDirectory(reservationNo)
        )) {
            throw new ReservationImageException(
                    "잘못된 이미지 경로입니다."
            );
        }

        if (!Files.exists(filePath)
                || !Files.isRegularFile(filePath)) {
            throw new ReservationImageNotFoundException(
                    reservationImageNo
            );
        }

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            image.getContentType()
                    );
        } catch (Exception e) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return new ReservationImageContent(
                new FileSystemResource(filePath),
                mediaType
        );
    }

    private void deleteInternal(
            Long reservationNo,
            Long reservationImageNo
    ) {
        ReservationImage image =
                requireImage(
                        reservationNo,
                        reservationImageNo
                );

        Path filePath =
                reservationDirectory(reservationNo)
                        .resolve(
                                image.getStoredFileName()
                        )
                        .normalize();

        reservationImageRepository.delete(image);
        reservationImageRepository.flush();

        deleteQuietly(filePath);
    }

    private ReservationImage requireImage(
            Long reservationNo,
            Long reservationImageNo
    ) {
        ReservationImage image =
                reservationImageRepository
                        .findById(reservationImageNo)
                        .orElseThrow(
                                () ->
                                        new ReservationImageNotFoundException(
                                                reservationImageNo
                                        )
                        );

        if (!image.getReservation()
                .getReservationNo()
                .equals(reservationNo)) {
            throw new ReservationImageAccessDeniedException();
        }

        return image;
    }

    private Reservation requireReservation(
            Long reservationNo
    ) {
        return reservationRepository
                .findById(reservationNo)
                .orElseThrow(
                        () ->
                                new ReservationNotFoundException(
                                        reservationNo
                                )
                );
    }

    private Reservation requireMemberReservation(
            String memberId,
            Long reservationNo
    ) {
        if (memberId == null
                || memberId.isBlank()
                || "anonymousUser".equals(memberId)) {
            throw new ReservationImageAccessDeniedException();
        }

        MemberReservationInfo member =
                reservationMemberReader
                        .findMemberInfoByMemberId(
                                memberId
                        )
                        .orElseThrow(
                                ReservationImageAccessDeniedException::new
                        );

        return reservationRepository
                .findByReservationNoAndMemberNo(
                        reservationNo,
                        member.getMemberNo()
                )
                .orElseThrow(
                        ReservationImageAccessDeniedException::new
                );
    }

    private Reservation requireGuestReservation(
            Long reservationNo,
            String guestPhone
    ) {
        String normalized =
                normalizeGuestPhone(guestPhone);

        return reservationRepository
                .findByReservationNoAndCustomerTypeAndGuestPhone(
                        reservationNo,
                        CustomerType.GUEST,
                        normalized
                )
                .orElseThrow(
                        ReservationImageAccessDeniedException::new
                );
    }

    private Path reservationDirectory(
            Long reservationNo
    ) {
        return Path.of(
                        uploadDir,
                        "reservation",
                        String.valueOf(reservationNo)
                )
                .toAbsolutePath()
                .normalize();
    }

    private void validateFile(
            MultipartFile file
    ) {
        if (file == null
                || file.isEmpty()) {
            throw new ReservationImageException(
                    "업로드할 이미지가 없습니다."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ReservationImageException(
                    "이미지 크기는 10MB를 초과할 수 없습니다."
            );
        }

        if (file.getContentType() == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        file.getContentType()
                )) {
            throw new ReservationImageException(
                    "JPG, PNG, WEBP 이미지만 업로드할 수 있습니다."
            );
        }
    }

    private String normalizeGuestPhone(
            String value
    ) {
        return value == null
                ? ""
                : value.replaceAll("\\D", "");
    }

    private String safeOriginalFileName(
            String fileName
    ) {
        if (fileName == null
                || fileName.isBlank()) {
            return "image";
        }

        return Path.of(fileName)
                .getFileName()
                .toString();
    }

    private String getExtension(
            String fileName
    ) {
        int index =
                fileName.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return fileName
                .substring(index)
                .toLowerCase();
    }

    private void deleteQuietly(
            Path path
    ) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
