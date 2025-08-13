package com.team12.userservice;

import com.team12.userservice.controller.UserController;
import com.team12.userservice.dto.*;
import com.team12.userservice.model.*;
import com.team12.userservice.service.AdminService;
import com.team12.userservice.service.AgentService;
import com.team12.userservice.service.TenantService;
import com.team12.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private AdminService adminService;
    @Mock
    private AgentService agentService;
    @Mock
    private TenantService tenantService;
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Jwt mockJwt(String role, String sub) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("https://smr.com/roles", List.of(role))
                .claim("sub", sub)
                .build();
    }

    @Test
    void login_ShouldReturnLoginCompleteDto() {
        Jwt jwt = mockJwt("TENANT", "sub123");
        UserRegisterDto dto = new UserRegisterDto();
        LoginCompleteDto expected = new LoginCompleteDto();
        when(userService.loginOrRegister(eq(dto), eq(Role.TENANT))).thenReturn(expected);

        ResponseEntity<LoginCompleteDto> response = userController.login(jwt, dto);

        assertThat(response.getBody()).isEqualTo(expected);
        verify(userService).loginOrRegister(dto, Role.TENANT);
    }

    @Test
    void reassignAgentRole_ShouldReturnAgent() {
        AssignNewRoleDto dto = new AssignNewRoleDto();
        Tenant tenant = new Tenant();
        Agent agent = new Agent();
        when(tenantService.findAndMarkRemoval(dto.getSub())).thenReturn(tenant);
        when(agentService.reassignToAgent(tenant)).thenReturn(agent);

        ResponseEntity<Agent> response = userController.reassignAgentRole(dto);

        assertThat(response.getBody()).isEqualTo(agent);
    }

    @Test
    void getFullUserInfoByOidcSub_Admin_ShouldReturnUser() {
        Jwt jwt = mockJwt("ADMIN", "adminSub");
        BaseUser user = new Tenant();
        when(userService.getUserByOidcSub("targetSub")).thenReturn(user);

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "targetSub");

        assertThat(response.getBody()).isEqualTo(user);
    }

    @Test
    void getAgentInfoById_ShouldReturnNotFound_WhenNull() {
        when(agentService.getAgentById(1L)).thenReturn(null);

        ResponseEntity<List<String>> response = userController.getAgentInfoById(1L);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void updateUserStatus_ShouldReturnError_WhenExceptionThrown() throws Exception {
        UserStatusUpdateDto dto = new UserStatusUpdateDto();
        dto.setUserId(5L);
        dto.setEnabled(true);
        when(userService.updateUserStatus(anyLong(), anyBoolean())).thenThrow(new RuntimeException("fail"));

        ResponseEntity<BaseUser> response = userController.updateUserStatus(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    void updateUserInfo_ShouldReturnNotFound_WhenDifferentUser() {
        Jwt jwt = mockJwt("TENANT", "sub123");
        UserInfoUpdateDto dto = new UserInfoUpdateDto();
        dto.setOidcSub("otherSub");

        ResponseEntity<BaseUser> response = userController.updateUserInfo(jwt, dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void submitApplication_ShouldReturnInternalServerError_OnException() throws Exception {
        IdentityVerificationSubmitDto dto = new IdentityVerificationSubmitDto();
        when(userService.submitIdentityVerification(dto)).thenThrow(new RuntimeException("error"));

        ResponseEntity<IdentityVerification> response = userController.submitApplication(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    void reviewApplication_ShouldReturnInternalServerError_WhenSetAgentVerifiedFails() throws Exception {
        IdentityVerificationReviewDto dto = new IdentityVerificationReviewDto();
        IdentityVerification iv = new IdentityVerification();
        iv.setStatus(VerificationStatus.APPROVED);
        iv.setAgentAuth0Id("agent1");

        when(userService.reviewIdentityVerification(dto)).thenReturn(iv);
        when(userService.setAgentVerified("agent1")).thenReturn(null);

        ResponseEntity<IdentityVerification> response = userController.reviewApplication(dto);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
    }
}
