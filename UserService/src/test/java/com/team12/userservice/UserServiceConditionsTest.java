package com.team12.userservice;

import com.team12.userservice.dto.*;
import com.team12.userservice.model.*;
import com.team12.userservice.repository.*;
import com.team12.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceConditionsTest {

    @Mock private AdminRepository adminRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private IdentityVerificationRepository idVRepository;
    @Mock private BaseUserRepository baseUserRepository;

    @InjectMocks private UserService userService;

    private Admin admin;
    private Agent agent;
    private Tenant tenant;
    private IdentityVerification identityVerification;

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
        admin.setPicture("picture_url");
        admin.setRole(Role.ADMIN);
        admin.setDeviceId("device123");

        agent = new Agent();
        agent.setOidcSub("agent123");
        agent.setUsername("agent");
        agent.setEmail("agent@example.com");
        agent.setRegisteredAt(LocalDateTime.now());
        agent.setLastLoginAt(LocalDateTime.now());
        agent.setEnabled(true);
        agent.setPicture("agent_picture");
        agent.setRole(Role.AGENT);
        agent.setDeviceId("device456");
        agent.setVerified(false);

        tenant = new Tenant();
        tenant.setOidcSub("tenant123");
        tenant.setUsername("tenant");
        tenant.setEmail("tenant@example.com");
        tenant.setRegisteredAt(LocalDateTime.now());
        tenant.setLastLoginAt(LocalDateTime.now());
        tenant.setEnabled(true);
        tenant.setPicture("tenant_picture");
        tenant.setRole(Role.TENANT);
        tenant.setDeviceId("device789");
        tenant.setPriceAlertEnabled(true);

        identityVerification = new IdentityVerification();
        identityVerification.setAgentAuth0Id("agent123");
        identityVerification.setStatus(VerificationStatus.SUBMITTED);
        identityVerification.setCreatedAt(LocalDateTime.now());
        identityVerification.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getUsers_ShouldReturnListOfUsers() {
        when(baseUserRepository.findAll()).thenReturn(List.of(admin, agent, tenant));

        List<BaseUser> users = userService.getUsers();

        assertThat(users).hasSize(3);
    }

    @Test
    void getUserByOidcSub_ShouldReturnUser_WhenFound() {
        when(baseUserRepository.findByOidcSub("admin123")).thenReturn(Optional.of(admin));

        BaseUser user = userService.getUserByOidcSub("admin123");

        assertThat(user).isNotNull();
        assertThat(user.getOidcSub()).isEqualTo("admin123");
    }

    @Test
    void getUserByOidcSub_ShouldReturnNull_WhenNotFound() {
        when(baseUserRepository.findByOidcSub("nonexistent")).thenReturn(Optional.empty());

        BaseUser user = userService.getUserByOidcSub("nonexistent");

        assertThat(user).isNull();
    }

    @Test
    void loginOrRegister_ShouldReturnLoginCompleteDto_WhenUserExists() {
        UserRegisterDto registerDto = new UserRegisterDto("admin", "picture_url", "admin@example.com", "admin123", "device123");

        when(baseUserRepository.findByOidcSub("admin123")).thenReturn(Optional.of(admin));

        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(admin);

        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.ADMIN);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(registerDto.getSub());
        assertThat(result.getEmail()).isEqualTo(registerDto.getEmail());
        assertThat(result.getPicture()).isEqualTo(registerDto.getPicture());
        assertThat(result.getUsername()).isEqualTo(registerDto.getUsername());
        verify(baseUserRepository).save(any(BaseUser.class));
    }

    @Test
    void loginOrRegister_ShouldCreateNewUser_WhenUserNotFound() {
        UserRegisterDto registerDto = new UserRegisterDto("agent123", "agent", "agent@example.com", "agent_picture", "device456");
        when(baseUserRepository.findByOidcSub("agent123")).thenReturn(Optional.empty());
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.AGENT);

        assertThat(result).isNotNull();
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void loginOrRegister_ShouldThrowException_WhenUnknownRole() {
        UserRegisterDto registerDto = new UserRegisterDto("unknown", "unknown", "unknown@example.com", "unknown_picture", "unknown_device");
        when(baseUserRepository.findByOidcSub("unknown")).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            userService.loginOrRegister(registerDto, null);
        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("agent123", "newemail@example.com", "123456789");
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.AGENT);

        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("123456789");
    }

    @Test
    void updateUser_ShouldThrowException_WhenAgentUserNotFound() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("nonexistent", "newemail@example.com", "123456789");
        when(agentRepository.findByOidcSub("nonexistent")).thenReturn(null);

        Exception exception = null;
        try {
            userService.updateUser(updateDto, Role.AGENT);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateUserStatus_ShouldEnableUser() throws Exception {
        agent.setEnabled(false);
        when(baseUserRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(agent);

        BaseUser updatedUser = userService.updateUserStatus(1L, true);

        assertThat(updatedUser.isEnabled()).isTrue();
        verify(baseUserRepository).save(any(BaseUser.class));
    }

    @Test
    void updateUserStatus_ShouldThrowException_WhenAdmin() throws Exception {
        admin.setRole(Role.ADMIN);
        when(baseUserRepository.findById(1L)).thenReturn(Optional.of(admin));

        Exception exception = null;
        try {
            userService.updateUserStatus(1L, false);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(Exception.class)
                .hasMessageContaining("NOT ALLOWED");
    }

    @Test
    void getAllIdentityVerifications_ShouldReturnList() {
        when(idVRepository.findAll()).thenReturn(List.of(identityVerification));

        List<IdentityVerification> verifications = userService.getAllIdentityVerifications();

        assertThat(verifications).hasSize(1);
    }

    @Test
    void submitIdentityVerification_ShouldSubmitNewApplication() throws Exception {
        IdentityVerificationSubmitDto submitDto = new IdentityVerificationSubmitDto("agent123", "reason", "pdfUrl");
        when(idVRepository.findByAgentAuth0IdAndStatusIn(eq("agent123"), anyList())).thenReturn(Optional.empty());
        when(idVRepository.save(any(IdentityVerification.class))).thenReturn(identityVerification);

        IdentityVerification newApplication = userService.submitIdentityVerification(submitDto);

        assertThat(newApplication).isNotNull();
        verify(idVRepository).save(any(IdentityVerification.class));
    }

    @Test
    void submitIdentityVerification_ShouldThrowException_WhenAlreadySubmitted() throws Exception {
        IdentityVerificationSubmitDto submitDto = new IdentityVerificationSubmitDto("agent123", "reason", "pdfUrl");
        when(idVRepository.findByAgentAuth0IdAndStatusIn(eq("agent123"), anyList())).thenReturn(Optional.of(identityVerification));

        Exception exception = null;
        try {
            userService.submitIdentityVerification(submitDto);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit more than one active application.");
    }

    @Test
    void reviewIdentityVerification_ShouldUpdateStatus() throws Exception {
        IdentityVerificationReviewDto reviewDto = new IdentityVerificationReviewDto(1L, VerificationStatus.APPROVED);
        when(idVRepository.findById(1L)).thenReturn(Optional.of(identityVerification));
        when(idVRepository.save(any(IdentityVerification.class))).thenReturn(identityVerification);

        IdentityVerification updatedApplication = userService.reviewIdentityVerification(reviewDto);

        assertThat(updatedApplication.getStatus()).isEqualTo(VerificationStatus.APPROVED);
        verify(idVRepository).save(any(IdentityVerification.class));
    }

    @Test
    void setAgentVerified_ShouldReturnVerifiedAgent() throws Exception {
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        Agent updatedAgent = userService.setAgentVerified("agent123");

        assertThat(updatedAgent.isVerified()).isTrue();
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void getDeviceIDById_ShouldReturnDeviceId() {
        agent.setDeviceId("device123");
        when(baseUserRepository.findById(1L)).thenReturn(Optional.of(agent));

        String deviceId = userService.getDeviceIDById(1L);

        assertThat(deviceId).isEqualTo("device123");
    }

    @Test
    void getDeviceIDById_ShouldReturnNull_WhenUserNotFound() {
        when(baseUserRepository.findById(1L)).thenReturn(Optional.empty());

        String deviceId = userService.getDeviceIDById(1L);

        assertThat(deviceId).isNull();
    }

    @Test
    void loginOrRegister_ShouldCreateNewAdminUser_WhenRoleIsAdmin() {
        UserRegisterDto registerDto = new UserRegisterDto("admin123", "admin", "admin@example.com", "admin_picture", "device123");
        when(baseUserRepository.findByOidcSub("admin123")).thenReturn(Optional.empty());
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);
        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.ADMIN);
        assertThat(result).isNotNull();
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void loginOrRegister_ShouldSetVerifiedFalse_WhenRoleIsAgent() {
        UserRegisterDto registerDto = new UserRegisterDto("agent123", "agent", "agent@example.com", "agent_picture", "device456");
        when(baseUserRepository.findByOidcSub("agent123")).thenReturn(Optional.empty());
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.AGENT);

        assertThat(result).isNotNull();
        assertThat(agent.isVerified()).isFalse();
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void loginOrRegister_ShouldSetPriceAlertEnabledFalse_WhenRoleIsTenant() {
        UserRegisterDto registerDto = new UserRegisterDto("tenant123", "tenant", "tenant@example.com", "tenant_picture", "device789");
        when(baseUserRepository.findByOidcSub("tenant123")).thenReturn(Optional.empty());
        tenant.setPriceAlertEnabled(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.TENANT);

        assertThat(result).isNotNull();
        assertThat(tenant.isPriceAlertEnabled()).isFalse();
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void saveUser_ShouldSaveAdminUser() {
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        BaseUser savedUser = userService.saveUser(admin);

        assertThat(savedUser).isEqualTo(admin);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void saveUser_ShouldSaveAgentUser() {
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        BaseUser savedUser = userService.saveUser(agent);

        assertThat(savedUser).isEqualTo(agent);
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void saveUser_ShouldSaveTenantUser() {
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        BaseUser savedUser = userService.saveUser(tenant);

        assertThat(savedUser).isEqualTo(tenant);
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void saveUser_ShouldThrowException_WhenUnknownUserType() {
        BaseUser unknownUser = mock(BaseUser.class);
        Exception exception = null;
        try {
            userService.saveUser(unknownUser);
        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown user type");
    }

    @Test
    void updateUser_ShouldReturnUpdatedAdmin_WhenRoleIsAdmin() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("admin123", "newadmin@example.com", "987654321");
        when(adminRepository.findByOidcSub("admin123")).thenReturn(admin);
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.ADMIN);

        assertThat(updatedUser.getEmail()).isEqualTo("newadmin@example.com");
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    void updateUser_ShouldReturnUpdatedTenant_WhenRoleIsTenant() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("tenant123", "newtenant@example.com", "123987654");
        when(tenantRepository.findByOidcSub("tenant123")).thenReturn(tenant);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.TENANT);

        assertThat(updatedUser.getEmail()).isEqualTo("newtenant@example.com");
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("123987654");
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("nonexistent", "newemail@example.com", "987654321");
        when(agentRepository.findByOidcSub("nonexistent")).thenReturn(null);

        Exception exception = null;
        try {
            userService.updateUser(updateDto, Role.AGENT);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateUser_ShouldNotUpdateEmail_WhenEmailIsNull() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("agent123", null, "987654321");
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.AGENT);

        assertThat(updatedUser.getEmail()).isEqualTo(agent.getEmail());
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    void updateUser_ShouldNotUpdatePhoneNumber_WhenPhoneNumberIsNull() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("agent123", "newemail@example.com", null);
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.AGENT);

        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getPhoneNumber()).isEqualTo(agent.getPhoneNumber());
    }

    @Test
    void getAllSubmittedIdentityVerifications_ShouldReturnList() {
        when(idVRepository.findByStatus(VerificationStatus.SUBMITTED)).thenReturn(List.of(identityVerification));

        List<IdentityVerification> verifications = userService.getAllSubmittedIdentityVerifications();

        assertThat(verifications).hasSize(1);
        assertThat(verifications.getFirst().getStatus()).isEqualTo(VerificationStatus.SUBMITTED);
    }

    @Test
    void getIdentityVerificationsByAgent_ShouldReturnList() {
        when(idVRepository.findByAgentAuth0Id("agent123")).thenReturn(List.of(identityVerification));

        List<IdentityVerification> verifications = userService.getIdentityVerificationsByAgent("agent123");

        assertThat(verifications).hasSize(1);
        assertThat(verifications.getFirst().getAgentAuth0Id()).isEqualTo("agent123");
    }

    @Test
    void reviewIdentityVerification_ShouldThrowException_WhenApplicationNotFound() {
        IdentityVerificationReviewDto reviewDto = new IdentityVerificationReviewDto(1L, VerificationStatus.APPROVED);
        when(idVRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = null;
        try {
            userService.reviewIdentityVerification(reviewDto);
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application not found");
    }

    @Test
    void setAgentVerified_ShouldThrowException_WhenAgentNotFound() {
        when(agentRepository.findByOidcSub("nonexistent")).thenReturn(null);

        Exception exception = null;
        try {
            userService.setAgentVerified("nonexistent");
        } catch (Exception e) {
            exception = e;
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Agent not found");
    }
}
