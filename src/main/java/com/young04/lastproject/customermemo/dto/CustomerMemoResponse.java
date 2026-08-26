package com.young04.lastproject.customermemo.dto;

import com.young04.lastproject.customermemo.entity.CustomerMemo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerMemoResponse {

    private Long memoId;

    private Long customerId;

    private String memoType;

    private String memoContent;

    private String importantYn;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public static CustomerMemoResponse from(
            CustomerMemo memo
    ) {

        return CustomerMemoResponse.builder()
                .memoId(memo.getMemoId())
                .customerId(
                        memo.getCustomer().getCustomerId()
                )
                .memoType(memo.getMemoType())
                .memoContent(memo.getMemoContent())
                .importantYn(memo.getImportantYn())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }
}