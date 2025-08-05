package com.team12.userservice.repository;

import com.team12.userservice.model.IdentityVerification;
import com.team12.userservice.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {
    // Find all idv by AgentAuth0Id
    List<IdentityVerification> findByAgentAuth0Id(String agentAuth0Id);

    // Find add idv by one Agent of status xxx
    Optional<IdentityVerification> findByAgentAuth0IdAndStatusIn(String agentAuth0Id, List<VerificationStatus> statuses);

    // Find all idv by status
    List<IdentityVerification> findByStatus(VerificationStatus status);
}
