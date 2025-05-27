package com.team12.userservice.repository;

import com.team12.userservice.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Tenant findByOidcSub(String oidcSub);
}
