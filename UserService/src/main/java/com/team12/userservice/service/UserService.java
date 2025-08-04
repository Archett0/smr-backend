package com.team12.userservice.service;

import com.team12.userservice.dto.LoginCompleteDto;
import com.team12.userservice.dto.UserInfoUpdateDto;
import com.team12.userservice.dto.UserRegisterDto;
import com.team12.userservice.model.*;
import com.team12.userservice.repository.AdminRepository;
import com.team12.userservice.repository.AgentRepository;
import com.team12.userservice.repository.BaseUserRepository;
import com.team12.userservice.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final BaseUserRepository baseUserRepository;
    private final AdminRepository adminRepository;
    private final AgentRepository agentRepository;
    private final TenantRepository tenantRepository;

    public UserService(BaseUserRepository baseUserRepository,
                       AdminRepository adminRepository,
                       AgentRepository agentRepository,
                       TenantRepository tenantRepository) {
        this.baseUserRepository = baseUserRepository;
        this.adminRepository = adminRepository;
        this.agentRepository = agentRepository;
        this.tenantRepository = tenantRepository;
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
}
