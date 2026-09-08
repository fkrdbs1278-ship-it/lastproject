package com.young04.lastproject.customermemo.entity;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "CUSTOMER_MEMO")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMO_ID")
    private Long memoId;


    // CUSTOMER_MEMO.CUSTOMER_ID
    // 같은 3번 담당 영역의 CustomerProfile과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CUSTOMER_ID",
            nullable = false
    )
    private CustomerProfile customer;


    // COUNSELING / ALLERGY / SCALP / HAIR / GENERAL
    @Column(
            name = "MEMO_TYPE",
            nullable = false,
            length = 20
    )
    private String memoType;


    @Column(
            name = "MEMO_CONTENT",
            nullable = false,
            length = 2000
    )
    private String memoContent;


    // 중요 특이사항 여부 Y / N
    // Oracle 실제 타입이 CHAR(1)이므로 명시적으로 CHAR 매핑
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "IMPORTANT_YN",
            nullable = false,
            length = 1,
            columnDefinition = "CHAR(1)"
    )
    private String importantYn;


    @Column(
            name = "CREATED_AT",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "UPDATED_AT",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;


    // 상담 메모 생성
    public static CustomerMemo create(
            CustomerProfile customer,
            String memoType,
            String memoContent,
            String importantYn
    ) {

        CustomerMemo memo = new CustomerMemo();

        memo.customer = customer;
        memo.memoType = memoType;
        memo.memoContent = memoContent;
        memo.importantYn = importantYn;

        return memo;
    }


    // 상담 메모 수정
    public void update(
            String memoType,
            String memoContent,
            String importantYn
    ) {

        this.memoType = memoType;
        this.memoContent = memoContent;
        this.importantYn = importantYn;
    }
}