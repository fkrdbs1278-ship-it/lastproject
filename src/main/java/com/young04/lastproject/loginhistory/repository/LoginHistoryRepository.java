package com.young04.lastproject.loginhistory.repository;

import com.young04.lastproject.loginhistory.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long> {
}
