package com.team12.userservice;

import com.team12.userservice.dto.TenantPrefUpdateDto;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.TenantRepository;
import com.team12.userservice.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    @Mock private TenantRepository tenantRepository;

    @InjectMocks private TenantService tenantService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize model objects for tests
        tenant = new Tenant();
        tenant.setOidcSub("tenant123");
        tenant.setUsername("tenant");
        tenant.setEmail("tenant@example.com");
        tenant.setRegisteredAt(LocalDateTime.now());
        tenant.setLastLoginAt(LocalDateTime.now());
        tenant.setEnabled(true);
        tenant.setPicture("tenant_picture");
        tenant.setRole(Role.TENANT);
        tenant.setPriceAlertEnabled(true);
    }

    @Test
    void addTenant_ShouldReturnAddedTenant() {
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        Tenant result = tenantService.addTenant(tenant);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(tenant.getOidcSub());
        assertThat(result.getUsername()).isEqualTo(tenant.getUsername());
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void getAllTenants_ShouldReturnListOfTenants() {
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        List<Tenant> tenants = tenantService.getAllTenants();

        assertThat(tenants).hasSize(1);
        assertThat(tenants.get(0)).isEqualTo(tenant);
    }

    @Test
    void getTenantById_ShouldReturnTenant_WhenFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        Tenant result = tenantService.getTenantById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(tenant.getOidcSub());
    }

    @Test
    void getTenantById_ShouldReturnNull_WhenNotFound() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.empty());

        Tenant result = tenantService.getTenantById(1L);

        assertThat(result).isNull();
    }

    @Test
    void getTenantByOidcSub_ShouldReturnTenant_WhenFound() {
        when(tenantRepository.findByOidcSub("tenant123")).thenReturn(tenant);

        Tenant result = tenantService.getTenantByOidcSub("tenant123");

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo("tenant123");
    }

    @Test
    void getTenantByOidcSub_ShouldReturnNull_WhenNotFound() {
        when(tenantRepository.findByOidcSub("tenant123")).thenReturn(null);

        Tenant result = tenantService.getTenantByOidcSub("tenant123");

        assertThat(result).isNull();
    }

    @Test
    void updateTenant_ShouldReturnUpdatedTenant() {
        tenant.setUsername("newTenant");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        Tenant result = tenantService.updateTenant(tenant);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newTenant");
    }

    @Test
    void deleteTenantById_ShouldCallDeleteMethod() {
        doNothing().when(tenantRepository).deleteById(1L);

        tenantService.deleteTenantById(1L);

        verify(tenantRepository).deleteById(1L);
    }

    @Test
    void findAndMarkRemoval_ShouldReturnDisabledTenant() {
        tenant.setEnabled(true);
        when(tenantRepository.findByOidcSub("tenant123")).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        Tenant result = tenantService.findAndMarkRemoval("tenant123");

        assertThat(result).isNotNull();
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void updatePreference_ShouldReturnUpdatedTenant() {
        TenantPrefUpdateDto updateDto = new TenantPrefUpdateDto("tenant123", false);
        when(tenantRepository.findByOidcSub("tenant123")).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        Tenant result = tenantService.updatePreference(updateDto);

        assertThat(result).isNotNull();
        assertThat(result.isPriceAlertEnabled()).isFalse();
    }
}
