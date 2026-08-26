package com.young04.lastproject.customergrade.repository;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerGradeRepository
        extends JpaRepository<CustomerGrade, String> {

    List<CustomerGrade> findAllByOrderByGradePriorityAsc();
}