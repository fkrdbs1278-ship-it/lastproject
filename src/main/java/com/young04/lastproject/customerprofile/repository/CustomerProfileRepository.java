package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository
        extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByPhone(String phone);

    Optional<CustomerProfile> findByMemberNo(Long memberNo);

    boolean existsByPhone(String phone);
}