package com.team12.userservice.repository;

import com.team12.userservice.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Agent findByOidcSub(String oidcSub);
}
