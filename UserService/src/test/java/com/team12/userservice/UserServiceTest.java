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

class UserServiceTest {

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
    void loginOrRegister_ShouldReturnLoginCompleteDto_WhenUserExists() {
        // Arrange
        UserRegisterDto registerDto = new UserRegisterDto("admin", "picture_url", "admin@example.com", "admin123", "device123");

        // Mock the existing user scenario: user already exists in the repository
        when(baseUserRepository.findByOidcSub("admin123")).thenReturn(Optional.of(admin));

        // Mock the save call, which will be invoked when we update the existing user
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(admin);

        // Act
        LoginCompleteDto result = userService.loginOrRegister(registerDto, Role.ADMIN);

        // Assert
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
    void updateUser_ShouldReturnUpdatedUser() {
        UserInfoUpdateDto updateDto = new UserInfoUpdateDto("agent123", "newemail@example.com", "123456789");
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        BaseUser updatedUser = userService.updateUser(updateDto, Role.AGENT);

        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getPhoneNumber()).isEqualTo("123456789");
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
}
