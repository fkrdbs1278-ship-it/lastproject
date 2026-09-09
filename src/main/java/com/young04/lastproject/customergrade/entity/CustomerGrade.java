package com.young04.lastproject.customergrade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "CUSTOMER_GRADE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerGrade {

    @Id
    @Column(name = "GRADE_CODE", length = 20)
    private String gradeCode;

    @Column(name = "GRADE_NAME", nullable = false, length = 30)
    private String gradeName;

    @Column(name = "GRADE_DESCRIPTION", length = 200)
    private String gradeDescription;

    @Column(name = "GRADE_PRIORITY", nullable = false)
    private Integer gradePriority;

    @Column(name = "CREATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}