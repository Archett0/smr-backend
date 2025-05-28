package com.team12.userservice.repository;

import com.team12.userservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByOidcSub(String oidcSub);
}
