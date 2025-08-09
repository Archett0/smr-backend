package com.team12.userservice.service;

import com.team12.userservice.dto.*;
import com.team12.userservice.model.*;
import com.team12.userservice.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final BaseUserRepository baseUserRepository;
    private final AdminRepository adminRepository;
    private final AgentRepository agentRepository;
    private final TenantRepository tenantRepository;
    private final IdentityVerificationRepository idVRepository;

    public UserService(BaseUserRepository baseUserRepository,
                       AdminRepository adminRepository,
                       AgentRepository agentRepository,
                       TenantRepository tenantRepository,
                       IdentityVerificationRepository idVRepository) {
        this.baseUserRepository = baseUserRepository;
        this.adminRepository = adminRepository;
        this.agentRepository = agentRepository;
        this.tenantRepository = tenantRepository;
        this.idVRepository = idVRepository;
    }

    public List<BaseUser> getUsers() {
        return baseUserRepository.findAll();
    }

    public BaseUser getUserByOidcSub(String oidcSub) {
        return baseUserRepository.findByOidcSub(oidcSub).orElse(null);
    }

    public LoginCompleteDto loginOrRegister(UserRegisterDto registerDto, Role role) {
        BaseUser user = baseUserRepository.findByOidcSub(registerDto.getSub()).orElse(null);
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            user.setDeviceId(registerDto.getDeviceId());
            baseUserRepository.save(user);
            return new LoginCompleteDto(user);
        }

        BaseUser newUser = switch (role) {
            case ADMIN -> new Admin();
            case AGENT -> new Agent();
            case TENANT -> new Tenant();
        };

        newUser.setOidcSub(registerDto.getSub());
        newUser.setUsername(registerDto.getUsername());
        newUser.setEmail(registerDto.getEmail());
        newUser.setRegisteredAt(LocalDateTime.now());
        newUser.setLastLoginAt(LocalDateTime.now());
        newUser.setEnabled(true);
        newUser.setPicture(registerDto.getPicture());
        newUser.setRole(role);
        newUser.setDeviceId(registerDto.getDeviceId());

        if (newUser instanceof Agent agent) {
            agent.setVerified(false);
        }
        else if (newUser instanceof Tenant tenant) {
            tenant.setPriceAlertEnabled(false);
        }

        BaseUser savedUser = saveUser(newUser);
        return new LoginCompleteDto(savedUser);
    }

    public BaseUser saveUser(BaseUser user) {
        return switch (user) {
            case Admin admin -> adminRepository.save(admin);
            case Agent agent -> agentRepository.save(agent);
            case Tenant tenant -> tenantRepository.save(tenant);
            case null, default -> throw new IllegalArgumentException("Unknown user type");
        };
    }

    public BaseUser updateUser(UserInfoUpdateDto dto, Role role) {
        String oidcSub = dto.getOidcSub();
        BaseUser user = switch (role) {
            case ADMIN -> adminRepository.findByOidcSub(oidcSub);
            case AGENT -> agentRepository.findByOidcSub(oidcSub);
            case TENANT -> tenantRepository.findByOidcSub(oidcSub);
            case null -> throw new IllegalArgumentException("Unknown user type");
        };
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        saveUser(user);
        return user;
    }

    public BaseUser updateUserStatus(Long userId, boolean enabled) throws Exception {
        BaseUser user = baseUserRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new Exception("NOT ALLOWED");
        }
        user.setEnabled(enabled);
        baseUserRepository.save(user);
        return user;
    }

    public List<IdentityVerification> getAllIdentityVerifications() {
        return idVRepository.findAll();
    }

    public List<IdentityVerification> getAllSubmittedIdentityVerifications() {
        return idVRepository.findByStatus(VerificationStatus.SUBMITTED);
    }

    public List<IdentityVerification> getIdentityVerificationsByAgent(String agentAuth0Id) {
        return idVRepository.findByAgentAuth0Id(agentAuth0Id);
    }

    public boolean canSubmitApplication(String agentAuth0Id) {
        List<VerificationStatus> statuses = List.of(VerificationStatus.SUBMITTED, VerificationStatus.APPROVED);
        Optional<IdentityVerification> existingApplication = idVRepository.findByAgentAuth0IdAndStatusIn(agentAuth0Id, statuses);
        return existingApplication.isEmpty();
    }

    public IdentityVerification submitIdentityVerification(IdentityVerificationSubmitDto submitDto) throws Exception {
        if (!canSubmitApplication(submitDto.getAgentAuth0Id())) {
            throw new IllegalStateException("Cannot submit more than one active application.");
        }
        IdentityVerification newApplication = new IdentityVerification();
        newApplication.setAgentAuth0Id(submitDto.getAgentAuth0Id());
        newApplication.setReason(submitDto.getReason());
        newApplication.setPdfUrl(submitDto.getPdfUrl());
        newApplication.setStatus(VerificationStatus.SUBMITTED);
        newApplication.setCreatedAt(LocalDateTime.now());
        newApplication.setUpdatedAt(LocalDateTime.now());
        return idVRepository.save(newApplication);
    }

    public IdentityVerification reviewIdentityVerification(IdentityVerificationReviewDto reviewDto) throws Exception {
        Optional<IdentityVerification> optionalApplication = idVRepository.findById(reviewDto.getId());
        if (optionalApplication.isEmpty()) {
            throw new IllegalArgumentException("Application not found");
        }
        IdentityVerification application = optionalApplication.get();
        application.setStatus(reviewDto.getStatus());
        application.setUpdatedAt(LocalDateTime.now());
        return idVRepository.save(application);
    }

    public Agent setAgentVerified(String agentAuth0Id) throws Exception {
        Agent agent = agentRepository.findByOidcSub(agentAuth0Id);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found");
        }
        agent.setVerified(true);
        return agentRepository.save(agent);
    }

    public String getDeviceIDById(Long id) {
        return baseUserRepository.findById(id)
                .map(BaseUser::getDeviceId)
                .orElse(null);
    }
}
