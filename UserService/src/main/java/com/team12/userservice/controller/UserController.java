package com.team12.userservice.controller;


import com.team12.userservice.dto.*;
import com.team12.userservice.model.*;
import com.team12.userservice.service.AdminService;
import com.team12.userservice.service.AgentService;
import com.team12.userservice.service.TenantService;
import com.team12.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.team12.userservice.util.UserServiceUtil.getOidcSubFromJwt;
import static com.team12.userservice.util.UserServiceUtil.getRoleFromJwt;

@RestController
@RequestMapping("/auth")
@Tag(name = "User Controller APIs", description = "SMR login, user info, etc.")
public class UserController {

    private final AdminService adminService;
    private final AgentService agentService;
    private final TenantService tenantService;
    private final UserService userService;

    public UserController(AdminService adminService,
                          AgentService agentService,
                          TenantService tenantService,
                          UserService userService) {
        this.adminService = adminService;
        this.agentService = agentService;
        this.tenantService = tenantService;
        this.userService = userService;
    }

    /**
     * Call after successful login/register
     *
     * @param jwt Access Token from request
     * @param dto Frontend to send this DTO
     * @return LoginCompleteDto
     */
    @PostMapping("/login")
    @Operation(summary = "Call this after user passed Auth0", description = "To get user info within SMR platform")
    public ResponseEntity<LoginCompleteDto> login(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRegisterDto dto) {
        Role role = getRoleFromJwt(jwt);
        LoginCompleteDto completeDto = userService.loginOrRegister(dto, role);
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

    /**
     * Get full user information by OIDC sub.
     * Only admin can get all user's data,
     * others may only get their own.
     * @param jwt access token
     * @param oidcSub OIDC sub
     * @return BaseUser generic type
     */
    @GetMapping("/fullUserBySub/{oidcSub}")
    public ResponseEntity<BaseUser> getFullUserInfoByOidcSub(@AuthenticationPrincipal Jwt jwt, @PathVariable String oidcSub) {
        Role role = getRoleFromJwt(jwt);
        String currentSub = getOidcSubFromJwt(jwt);
        switch (role) {
            case ADMIN:
                return ResponseEntity.ok(userService.getUserByOidcSub(oidcSub)); // admin should have full access
            case AGENT:
                if (Objects.equals(oidcSub, currentSub)) {
                    return ResponseEntity.ok(agentService.getAgentByOidcSub(oidcSub));
                } else {
                    return ResponseEntity.notFound().build();
                }
            case TENANT:
                if (Objects.equals(oidcSub, currentSub)) {
                    return ResponseEntity.ok(tenantService.getTenantByOidcSub(oidcSub));
                } else {
                    return ResponseEntity.notFound().build();
                }
            case null:
                throw new IllegalArgumentException("Unknown user type");
        }
    }

    /**
     * Get all users
     * @return Arraylist of all users
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/userList")
    public ResponseEntity<List<BaseUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    /**
     * Enable or disable a user
     * @param updateDto UserStatusUpdateDto
     * @return Updated baseUser
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/updateUserStatus")
    public ResponseEntity<BaseUser> updateUserStatus(@RequestBody UserStatusUpdateDto updateDto) {
        try {
            BaseUser user = userService.updateUserStatus(updateDto.getUserId(), updateDto.isEnabled());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update user's basic info
     * @param jwt access token
     * @param dto user info
     * @return BaseUser generic type
     */
    @PutMapping("/updateUserInfo")
    public ResponseEntity<BaseUser> updateUserInfo(@AuthenticationPrincipal Jwt jwt, @RequestBody UserInfoUpdateDto dto) {
        Role role = getRoleFromJwt(jwt);
        String currentSub = getOidcSubFromJwt(jwt);
        if (Objects.equals(currentSub, dto.getOidcSub())) {
            return ResponseEntity.ok(userService.updateUser(dto, role));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update tenant preferences
     * @param jwt access token
     * @param dto preferences
     * @return BaseUser generic type
     */
    @PutMapping("/updateTenantPref")
    public ResponseEntity<BaseUser> updateTenantPref(@AuthenticationPrincipal Jwt jwt, @RequestBody TenantPrefUpdateDto dto) {
        String currentSub = getOidcSubFromJwt(jwt);
        if (Objects.equals(currentSub, dto.getOidcSub())) {
            return ResponseEntity.ok(tenantService.updatePreference(dto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update agent preferences
     * @param jwt access token
     * @param dto preferences
     * @return BaseUser generic type
     */
    @PutMapping("/updateAgentPref")
    public ResponseEntity<BaseUser> updateAgentPref(@AuthenticationPrincipal Jwt jwt, @RequestBody AgentPrefUpdateDto dto) {
        String currentSub = getOidcSubFromJwt(jwt);
        if (Objects.equals(currentSub, dto.getOidcSub())) {
            return ResponseEntity.ok(agentService.updatePreference(dto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FOR TESTING ONLY
     * @return RE
     */
    @GetMapping("/reachableTest")
    public ResponseEntity<String> reachableTest() {
        return ResponseEntity.ok("Can!");
    }
}
