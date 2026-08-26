package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;

import java.util.List;

public interface CustomerProfileRepositoryCustom {

    List<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition
    );
}