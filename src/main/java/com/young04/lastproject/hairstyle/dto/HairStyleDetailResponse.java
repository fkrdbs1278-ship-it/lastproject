package com.young04.lastproject.hairstyle.dto;

import com.young04.lastproject.servicemenu.dto.ServiceMenuResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HairStyleDetailResponse {

    private HairStyleResponse hairStyle;

    private List<ServiceMenuResponse> recommendedServices;
}