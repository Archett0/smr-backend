package com.team12.userservice;

import com.team12.userservice.model.Admin;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.AdminRepository;
import com.team12.userservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock private AdminRepository adminRepository;

    @InjectMocks private AdminService adminService;

    private Admin admin;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize model objects for tests
        admin = new Admin();
        admin.setOidcSub("admin123");
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRegisteredAt(LocalDateTime.now());
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setEnabled(true);
        admin.setPicture("admin_picture");
        admin.setRole(Role.ADMIN);

        tenant = new Tenant();
        tenant.setOidcSub("tenant123");
        tenant.setUsername("tenant");
        tenant.setEmail("tenant@example.com");
        tenant.setRegisteredAt(LocalDateTime.now());
        tenant.setLastLoginAt(LocalDateTime.now());
        tenant.setEnabled(true);
        tenant.setPicture("tenant_picture");
        tenant.setRole(Role.TENANT);
    }

    @Test
    void addAdmin_ShouldReturnAddedAdmin() {
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        Admin result = adminService.addAdmin(admin);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(admin.getOidcSub());
        assertThat(result.getUsername()).isEqualTo(admin.getUsername());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void getAllAdmins_ShouldReturnListOfAdmins() {
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        List<Admin> admins = adminService.getAllAdmins();

        assertThat(admins).hasSize(1);
        assertThat(admins.get(0)).isEqualTo(admin);
    }

    @Test
    void getAdminById_ShouldReturnAdmin_WhenFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        Admin result = adminService.getAdminById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(admin.getOidcSub());
    }

    @Test
    void getAdminById_ShouldReturnNull_WhenNotFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());

        Admin result = adminService.getAdminById(1L);

        assertThat(result).isNull();
    }

    @Test
    void getAdminByOidcSub_ShouldReturnAdmin_WhenFound() {
        when(adminRepository.findByOidcSub("admin123")).thenReturn(admin);

        Admin result = adminService.getAdminByOidcSub("admin123");

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo("admin123");
    }

    @Test
    void getAdminByOidcSub_ShouldReturnNull_WhenNotFound() {
        when(adminRepository.findByOidcSub("admin123")).thenReturn(null);

        Admin result = adminService.getAdminByOidcSub("admin123");

        assertThat(result).isNull();
    }

    @Test
    void updateAdmin_ShouldReturnUpdatedAdmin() {
        admin.setUsername("newadmin");
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        Admin result = adminService.updateAdmin(admin);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newadmin");
    }

    @Test
    void deleteAdminById_ShouldCallDeleteMethod() {
        doNothing().when(adminRepository).deleteById(1L);

        adminService.deleteAdminById(1L);

        verify(adminRepository).deleteById(1L);
    }

    @Test
    void reassignToAdmin_ShouldReturnNewAdmin() {
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        Admin result = adminService.reassignToAdmin(tenant);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(admin.getOidcSub());
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }
}
