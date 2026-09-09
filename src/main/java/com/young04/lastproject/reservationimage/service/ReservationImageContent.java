package com.young04.lastproject.reservationimage.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ReservationImageContent(
        Resource resource,
        MediaType mediaType
) {
}
