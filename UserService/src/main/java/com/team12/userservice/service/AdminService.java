package com.team12.userservice.service;

import com.team12.userservice.model.Admin;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin addAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Admin getAdminByOidcSub(String oidcSub) {
        return adminRepository.findByOidcSub(oidcSub);
    }

    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public void deleteAdminById(Long id) {
        adminRepository.deleteById(id);
    }

    public Admin reassignToAdmin(Tenant tenant) {
        Admin newAdmin = new Admin();
        newAdmin.setOidcSub(tenant.getOidcSub());
        newAdmin.setUsername(tenant.getUsername());
        newAdmin.setEmail(tenant.getEmail());
        newAdmin.setRegisteredAt(tenant.getRegisteredAt());
        newAdmin.setLastLoginAt(tenant.getLastLoginAt());
        newAdmin.setEnabled(true);
        newAdmin.setPicture(tenant.getPicture());
        newAdmin.setRole(Role.ADMIN);
        return adminRepository.save(newAdmin);
    }
}
