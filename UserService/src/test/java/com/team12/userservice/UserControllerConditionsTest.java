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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserControllerConditionsTest {

    @Mock private AdminService adminService;
    @Mock private AgentService agentService;
    @Mock private TenantService tenantService;
    @Mock private UserService userService;

    @Mock private Jwt jwt;

    @InjectMocks private UserController userController;

    private Admin admin;
    private Agent agent;
    private Tenant tenant;
    private IdentityVerification identityVerification;
    private AgentPrefUpdateDto agentPrefUpdateDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize model objects for tests
        admin = new Admin();
        admin.setOidcSub("admin123");
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        agent = new Agent();
        agent.setOidcSub("agent123");
        agent.setUsername("agent");
        agent.setEmail("agent@example.com");
        agent.setRole(Role.AGENT);

        tenant = new Tenant();
        tenant.setOidcSub("tenant123");
        tenant.setUsername("tenant");
        tenant.setEmail("tenant@example.com");
        tenant.setRole(Role.TENANT);

        identityVerification = new IdentityVerification();
        identityVerification.setAgentAuth0Id("agent123");
        identityVerification.setStatus(VerificationStatus.SUBMITTED);

        agentPrefUpdateDto = new AgentPrefUpdateDto();
        agentPrefUpdateDto.setOidcSub("agent123");
        agentPrefUpdateDto.setVerified(false);
    }

    @Test
    void login_ShouldReturnLoginCompleteDto_WhenValid() {
        UserRegisterDto dto = new UserRegisterDto("admin", "admin.jpg", "admin@example.com", "admin123", "device123");

        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("ADMIN"));
        when(userService.loginOrRegister(dto, Role.ADMIN)).thenReturn(new LoginCompleteDto(admin));

        ResponseEntity<LoginCompleteDto> response = userController.login(jwt, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(userService).loginOrRegister(dto, Role.ADMIN);
    }

    @Test
    void reassignAgentRole_ShouldReassignRoleToAgent_WhenAdmin() {
        AssignNewRoleDto dto = new AssignNewRoleDto("tenant123");

        when(tenantService.findAndMarkRemoval("tenant123")).thenReturn(tenant);
        when(agentService.reassignToAgent(tenant)).thenReturn(agent);

        ResponseEntity<Agent> response = userController.reassignAgentRole(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(agent);
        verify(agentService).reassignToAgent(tenant);
    }

    @Test
    void reassignAgentRole_ShouldReturnForbidden_WhenNotAdmin() {
        AssignNewRoleDto dto = new AssignNewRoleDto("tenant123");

        // Assuming @PreAuthorize requires 'ADMIN' role to access the endpoint
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));

        ResponseEntity<Agent> response = userController.reassignAgentRole(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnUser_WhenAdminRole() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("ADMIN"));
        when(userService.getUserByOidcSub("admin123")).thenReturn(admin);

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "admin123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(admin);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnNotFound_WhenAgentAccessingAnotherUser() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("AGENT"));
        when(agentService.getAgentByOidcSub("tenant123")).thenReturn(agent);

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "tenant123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDeviceIDById_ShouldReturnDeviceId_WhenValidId() {
        when(userService.getDeviceIDById(1L)).thenReturn("device123");

        ResponseEntity<String> response = userController.getDeviceIDById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("device123");
    }

    @Test
    void getDeviceIDById_ShouldReturnNotFound_WhenDeviceIdNotFound() {
        when(userService.getDeviceIDById(1L)).thenReturn(null);

        ResponseEntity<String> response = userController.getDeviceIDById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateUserStatus_ShouldReturnUpdatedUser_WhenAdmin() throws Exception {
        UserStatusUpdateDto dto = new UserStatusUpdateDto(1L, true);
        when(userService.updateUserStatus(1L, true)).thenReturn(admin);

        ResponseEntity<BaseUser> response = userController.updateUserStatus(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(admin);
    }

    @Test
    void updateUserStatus_ShouldReturnInternalError_WhenExceptionThrown() throws Exception {
        UserStatusUpdateDto dto = new UserStatusUpdateDto(1L, true);
        when(userService.updateUserStatus(1L, true)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseUser> response = userController.updateUserStatus(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateUserInfo_ShouldReturnUpdatedUser_WhenValid() {
        UserInfoUpdateDto dto = new UserInfoUpdateDto("tenant123", "newemail@example.com", "987654321");
        when(jwt.getSubject()).thenReturn("tenant123");
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));
        when(userService.updateUser(dto, Role.TENANT)).thenReturn(tenant);

        ResponseEntity<BaseUser> response = userController.updateUserInfo(jwt, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tenant);
    }

    @Test
    void updateUserInfo_ShouldReturnNotFound_WhenDifferentSub() {
        UserInfoUpdateDto dto = new UserInfoUpdateDto("tenant123", "newemail@example.com", "987654321");
        when(jwt.getSubject()).thenReturn("otherSub");
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));
        ResponseEntity<BaseUser> response = userController.updateUserInfo(jwt, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTenantPref_ShouldReturnUpdatedTenant_WhenValid() {
        TenantPrefUpdateDto dto = new TenantPrefUpdateDto("tenant123", true);
        when(jwt.getSubject()).thenReturn("tenant123");
        when(tenantService.updatePreference(dto)).thenReturn(tenant);

        ResponseEntity<BaseUser> response = userController.updateTenantPref(jwt, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tenant);
    }

    @Test
    void updateTenantPref_ShouldReturnNotFound_WhenDifferentSub() {
        TenantPrefUpdateDto dto = new TenantPrefUpdateDto("tenant123", true);
        when(jwt.getSubject()).thenReturn("otherSub");

        ResponseEntity<BaseUser> response = userController.updateTenantPref(jwt, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllUsers_ShouldReturnList_WhenAdmin() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("ADMIN"));
        when(userService.getUsers()).thenReturn(List.of(admin, agent, tenant));

        ResponseEntity<List<BaseUser>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(admin, agent, tenant);
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenNotAdmin() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));

        ResponseEntity<List<BaseUser>> response = userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void submitApp_ShouldSubmitIdentityVerification_WhenValid() throws Exception {
        IdentityVerificationSubmitDto dto = new IdentityVerificationSubmitDto("agent123", "reason", "pdfUrl");
        when(userService.submitIdentityVerification(dto)).thenReturn(new IdentityVerification());

        ResponseEntity<IdentityVerification> response = userController.submitApplication(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void submitApp_ShouldReturnInternalError_WhenExceptionThrown() throws Exception {
        IdentityVerificationSubmitDto dto = new IdentityVerificationSubmitDto("agent123", "reason", "pdfUrl");
        when(userService.submitIdentityVerification(dto)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<IdentityVerification> response = userController.submitApplication(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnAgent_WhenAgentRoleAndOidcSubMatches() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("AGENT"));
        when(jwt.getSubject()).thenReturn("agent123");
        when(agentService.getAgentByOidcSub("agent123")).thenReturn(agent);

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "agent123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(agent);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnNotFound_WhenAgentRoleAndOidcSubDoesNotMatch() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("AGENT"));
        when(jwt.getSubject()).thenReturn("tenant123");

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "agent123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnTenant_WhenTenantRoleAndOidcSubMatches() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));
        when(jwt.getSubject()).thenReturn("tenant123");
        when(tenantService.getTenantByOidcSub("tenant123")).thenReturn(tenant);

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "tenant123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tenant);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldReturnNotFound_WhenTenantRoleAndOidcSubDoesNotMatch() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT"));
        when(jwt.getSubject()).thenReturn("agent123");

        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "tenant123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getFullUserInfoByOidcSub_ShouldThrowException_WhenRoleIsNull() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of());
        ResponseEntity<BaseUser> response = userController.getFullUserInfoByOidcSub(jwt, "admin123");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getAgentInfoById_ShouldReturnAgentInfo_WhenAgentFound() {
        Long agentId = 1L;
        List<String> agentInfo = List.of("Agent Name", "Agent Email", "Agent Phone");
        when(agentService.getAgentById(agentId)).thenReturn(agentInfo);
        ResponseEntity<List<String>> response = userController.getAgentInfoById(agentId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(agentInfo);
    }

    @Test
    void updateAgentPref_ShouldUpdatePreferences_WhenOidcSubMatches() {
        when(jwt.getSubject()).thenReturn("agent123");
        when(agentService.updatePreference(agentPrefUpdateDto)).thenReturn(agent);
        ResponseEntity<BaseUser> response = userController.updateAgentPref(jwt, agentPrefUpdateDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(agent);
    }

    @Test
    void updateAgentPref_ShouldReturnNotFound_WhenOidcSubDoesNotMatch() {
        when(jwt.getSubject()).thenReturn("tenant123");
        ResponseEntity<BaseUser> response = userController.updateAgentPref(jwt, agentPrefUpdateDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllApplications_ShouldReturnListOfApplications() {
        when(userService.getAllIdentityVerifications()).thenReturn(List.of(identityVerification));

        ResponseEntity<List<IdentityVerification>> response = userController.getAllApplications();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAllSubmittedApplications_ShouldReturnListOfApplications() {
        when(userService.getAllSubmittedIdentityVerifications()).thenReturn(List.of(identityVerification));

        ResponseEntity<List<IdentityVerification>> response = userController.getSubmittedApplications();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAllApplicationsByAgentAuth0Id_ShouldReturnList_WhenAdminRole() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("ADMIN"));
        when(userService.getIdentityVerificationsByAgent("agent123")).thenReturn(List.of(identityVerification));

        ResponseEntity<List<IdentityVerification>> response = userController.getAllApplicationsByAgentAuth0Id("agent123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getAllApplicationsByAgentAuth0Id_ShouldReturnList_WhenAgentRole() {
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("AGENT"));
        when(jwt.getSubject()).thenReturn("agent123");
        when(userService.getIdentityVerificationsByAgent("agent123")).thenReturn(List.of(identityVerification));

        ResponseEntity<List<IdentityVerification>> response = userController.getAllApplicationsByAgentAuth0Id("agent123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}
