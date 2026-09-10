package com.young04.lastproject.servicemenu.repository;

import com.young04.lastproject.servicemenu.entity.ServiceMenu;
import com.young04.lastproject.servicemenu.entity.ServiceMenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceMenuRepository extends JpaRepository<ServiceMenu, Long> {

    List<ServiceMenu> findByActiveYnOrderByDisplayOrderAscNoAsc(String activeYn);

    List<ServiceMenu> findByCategoryAndActiveYnOrderByDisplayOrderAscNoAsc(
            ServiceMenuCategory category,
            String activeYn
    );

    Optional<ServiceMenu> findByNoAndActiveYn(Long no, String activeYn);

    List<ServiceMenu> findAllByOrderByDisplayOrderAscNoAsc();
}
