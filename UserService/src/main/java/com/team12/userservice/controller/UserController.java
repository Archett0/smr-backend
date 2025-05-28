package com.team12.userservice.controller;


import com.team12.userservice.dto.AssignNewRoleDto;
import com.team12.userservice.dto.LoginCompleteDto;
import com.team12.userservice.dto.UserRegisterDto;
import com.team12.userservice.model.Admin;
import com.team12.userservice.model.Agent;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.service.AdminService;
import com.team12.userservice.service.AgentService;
import com.team12.userservice.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final AdminService adminService;
    private final AgentService agentService;
    private final TenantService tenantService;

    public UserController(AdminService adminService, AgentService agentService, TenantService tenantService) {
        this.adminService = adminService;
        this.agentService = agentService;
        this.tenantService = tenantService;
    }

    /**
     * Call after successful login/register
     *
     * @param jwt Access Token from request
     * @param dto Frontend to send this DTO
     * @return LoginCompleteDto
     */
    @PostMapping("/login")
    public ResponseEntity<LoginCompleteDto> login(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRegisterDto dto) {
        List<String> roles = jwt.getClaimAsStringList("https://smr.com/roles");
        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("User has no role assigned");
        }
        String roleStr = roles.getFirst();
        Role role = Role.valueOf(roleStr);
        LoginCompleteDto completeDto = switch (role) {
            case ADMIN -> adminService.loginOrRegister(dto);
            case AGENT -> agentService.loginOrRegister(dto);
            case TENANT -> tenantService.loginOrRegister(dto);
        };
        return ResponseEntity.ok(completeDto);
    }

    /**
     * Call to reassign AGENT role for TENANT
     *
     * @param dto Frontend to send this DTO
     * @return The new Agent instance
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/reassignAgentRole")
    public ResponseEntity<Agent> reassignAgentRole(@RequestBody AssignNewRoleDto dto) {
        Tenant tenant = tenantService.findAndMarkRemoval(dto.getSub());
        Agent agent = agentService.reassignToAgent(tenant);
        return ResponseEntity.ok(agent);
    }

    /**
     * Call to reassign ADMIN role for TENANT
     *
     * @param dto Frontend to send this DTO
     * @return The new Admin instance
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/reassignAdminRole")
    public ResponseEntity<Admin> reassignAdminRole(@RequestBody AssignNewRoleDto dto) {
        Tenant tenant = tenantService.findAndMarkRemoval(dto.getSub());
        Admin admin = adminService.reassignToAdmin(tenant);
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/reachableTest")
    public ResponseEntity<String> reachableTest() {
        return ResponseEntity.ok("Can!");
    }
}
