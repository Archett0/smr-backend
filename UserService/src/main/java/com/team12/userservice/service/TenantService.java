package com.team12.userservice.service;

import com.team12.userservice.dto.LoginCompleteDto;
import com.team12.userservice.dto.UserRegisterDto;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant addTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    public Tenant updateTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteTenantById(Long id) {
        tenantRepository.deleteById(id);
    }

    public LoginCompleteDto loginOrRegister(UserRegisterDto registerDto) {
        Tenant tenant = tenantRepository.findByOidcSub(registerDto.getSub());
        if (tenant != null) {
            tenant.setLastLoginAt(LocalDateTime.now());
            tenantRepository.save(tenant);
            return new LoginCompleteDto(tenant);
        }
        else {
            Tenant newTenant = new Tenant();
            newTenant.setOidcSub(registerDto.getSub());
            newTenant.setPriceAlertEnabled(false);
            newTenant.setUsername(registerDto.getUsername());
            newTenant.setEmail(registerDto.getEmail());
            newTenant.setRegisteredAt(LocalDateTime.now());
            newTenant.setLastLoginAt(LocalDateTime.now());
            newTenant.setEnabled(true);
            newTenant.setPicture(registerDto.getPicture());
            newTenant.setRole(Role.TENANT);
            Tenant savedTenant = tenantRepository.save(newTenant);
            return new LoginCompleteDto(savedTenant);
        }
    }

    public Tenant findAndMarkRemoval(String oidcSub) {
        Tenant tenant = tenantRepository.findByOidcSub(oidcSub);
        tenant.setEnabled(false);
        tenantRepository.save(tenant);
        return tenant;
    }
}
