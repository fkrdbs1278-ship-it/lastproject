package com.young04.lastproject.reservationimage.service;

import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.exception.ReservationNotFoundException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.entity.ReservationImage;
import com.young04.lastproject.reservationimage.exception.ReservationImageException;
import com.young04.lastproject.reservationimage.repository.ReservationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public ReservationImageResponse upload(
            Long reservationNo,
            MultipartFile file
    ) {
        validateFile(file);

        Reservation reservation =
                reservationRepository
                        .findById(reservationNo)
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                reservationNo
                                        )
                        );

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
                Path.of(
                        uploadDir,
                        "reservation",
                        String.valueOf(reservationNo)
                )
                .toAbsolutePath()
                .normalize();

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
            return ReservationImageResponse.from(
                    reservationImageRepository
                            .save(image)
            );
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    public List<ReservationImageResponse> getImages(
            Long reservationNo
    ) {
        return reservationImageRepository
                .findByReservationReservationNoOrderBySortOrderAsc(
                        reservationNo
                )
                .stream()
                .map(
                        ReservationImageResponse::from
                )
                .toList();
    }

    @Transactional
    public void delete(
            Long reservationNo,
            Long reservationImageNo
    ) {

        ReservationImage image =
                reservationImageRepository
                        .findById(reservationImageNo)
                        .orElseThrow(
                                () ->
                                        new ReservationImageException(
                                                "예약 사진을 찾을 수 없습니다."
                                        )
                        );

        if (!image.getReservation()
                .getReservationNo()
                .equals(reservationNo)) {

            throw new ReservationImageException(
                    "해당 예약의 이미지가 아닙니다."
            );
        }

        Path filePath =
                Path.of(
                                uploadDir,
                                "reservation",
                                String.valueOf(reservationNo),
                                image.getStoredFileName()
                        )
                        .toAbsolutePath()
                        .normalize();

        reservationImageRepository.delete(image);
        reservationImageRepository.flush();

        deleteQuietly(filePath);
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
