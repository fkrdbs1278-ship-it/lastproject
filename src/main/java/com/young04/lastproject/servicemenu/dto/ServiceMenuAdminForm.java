package com.young04.lastproject.servicemenu.dto;

import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceMenuAdminForm {

    @NotNull(message = "카테고리를 선택해주세요.")
    private ServiceMenuCategory category;

    @NotBlank(message = "시술명을 입력해주세요.")
    @Size(max = 100, message = "시술명은 100자 이내로 입력해주세요.")
    private String name;

    @Size(max = 1000, message = "설명은 1000자 이내로 입력해주세요.")
    private String description;

    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Long price;

    @NotNull(message = "소요시간을 입력해주세요.")
    @Min(value = 1, message = "소요시간은 1분 이상이어야 합니다.")
    @Max(value = 1440, message = "소요시간은 1440분 이내로 입력해주세요.")
    private Integer durationMin;

    @Size(max = 500, message = "이미지 URL은 500자 이내로 입력해주세요.")
    private String imageUrl;

    @NotNull(message = "사용 여부를 선택해주세요.")
    @Pattern(regexp = "Y|N", message = "사용 여부 값이 올바르지 않습니다.")
    private String activeYn = "Y";

    @NotNull(message = "표시 순서를 입력해주세요.")
    @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
    private Integer displayOrder = 0;

    public static ServiceMenuAdminForm from(ServiceMenu menu) {
        ServiceMenuAdminForm form = new ServiceMenuAdminForm();
        form.setCategory(menu.getCategory());
        form.setName(menu.getName());
        form.setDescription(menu.getDescription());
        form.setPrice(menu.getPrice());
        form.setDurationMin(menu.getDurationMin());
        form.setImageUrl(menu.getImageUrl());
        form.setActiveYn(menu.getActiveYn());
        form.setDisplayOrder(menu.getDisplayOrder());
        return form;
    }
}
