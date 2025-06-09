package com.team12.userservice.repository;

import com.team12.userservice.model.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseUserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByOidcSub(String oidcSub);
}
