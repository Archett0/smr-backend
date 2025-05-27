package com.team12.userservice.service;

import com.team12.userservice.dto.LoginCompleteDto;
import com.team12.userservice.dto.UserRegisterDto;
import com.team12.userservice.model.Admin;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public void deleteAdminById(Long id) {
        adminRepository.deleteById(id);
    }

    public LoginCompleteDto loginOrRegister(UserRegisterDto registerDto) {
        Admin admin = adminRepository.findByOidcSub(registerDto.getSub());
        if (admin != null) {
            admin.setLastLoginAt(LocalDateTime.now());
            adminRepository.save(admin);
            return new LoginCompleteDto(admin);
        }
        else {
            Admin newAdmin = new Admin();
            newAdmin.setOidcSub(registerDto.getSub());
            newAdmin.setUsername(registerDto.getUsername());
            newAdmin.setEmail(registerDto.getEmail());
            newAdmin.setRegisteredAt(LocalDateTime.now());
            newAdmin.setLastLoginAt(LocalDateTime.now());
            newAdmin.setEnabled(true);
            newAdmin.setPicture(registerDto.getPicture());
            newAdmin.setRole(Role.ADMIN);
            Admin savedAdmin = adminRepository.save(newAdmin);
            return new LoginCompleteDto(savedAdmin);
        }
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
