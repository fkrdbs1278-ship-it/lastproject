package com.young04.lastproject.customergrade.service;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import com.young04.lastproject.customergrade.repository.CustomerGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerGradeService {

    private final CustomerGradeRepository customerGradeRepository;

    // 고객 등급 전체 조회
    public List<CustomerGrade> findAllGrades() {

        log.info("고객 등급 전체 조회");

        return customerGradeRepository.findAllByOrderByGradePriorityAsc();
    }

    // 고객 등급 코드로 조회
    public Optional<CustomerGrade> findByGradeCode(String gradeCode) {

        log.info("고객 등급 조회 gradeCode={}", gradeCode);

        return customerGradeRepository.findById(gradeCode);
    }
}