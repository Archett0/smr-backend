package com.team12.userservice.service;

import com.team12.userservice.dto.TenantPrefUpdateDto;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.TenantRepository;
import org.springframework.stereotype.Service;

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

    public Tenant getTenantByOidcSub(String oidcSub) {
        return tenantRepository.findByOidcSub(oidcSub);
    }

    public Tenant updateTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteTenantById(Long id) {
        tenantRepository.deleteById(id);
    }

    public Tenant findAndMarkRemoval(String oidcSub) {
        Tenant tenant = tenantRepository.findByOidcSub(oidcSub);
        tenant.setEnabled(false);
        tenantRepository.save(tenant);
        return tenant;
    }

    public Tenant updatePreference(TenantPrefUpdateDto dto) {
        Tenant tenant = tenantRepository.findByOidcSub(dto.getOidcSub());
        tenant.setPriceAlertEnabled(dto.isPriceAlertEnabled());
        tenantRepository.save(tenant);
        return tenant;
    }
}
