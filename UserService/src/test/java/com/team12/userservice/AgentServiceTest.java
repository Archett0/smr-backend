package com.team12.userservice;

import com.team12.userservice.dto.AgentPrefUpdateDto;
import com.team12.userservice.model.Agent;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.AgentRepository;
import com.team12.userservice.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AgentServiceTest {

    @Mock private AgentRepository agentRepository;

    @InjectMocks private AgentService agentService;

    private Agent agent;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize model objects for tests
        agent = new Agent();
        agent.setOidcSub("agent123");
        agent.setUsername("agent");
        agent.setEmail("agent@example.com");
        agent.setRegisteredAt(LocalDateTime.now());
        agent.setLastLoginAt(LocalDateTime.now());
        agent.setEnabled(true);
        agent.setPicture("agent_picture");
        agent.setRole(Role.AGENT);

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
    void addAgent_ShouldReturnAddedAgent() {
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        Agent result = agentService.addAgent(agent);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(agent.getOidcSub());
        assertThat(result.getUsername()).isEqualTo(agent.getUsername());
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void getAllAgents_ShouldReturnListOfAgents() {
        when(agentRepository.findAll()).thenReturn(List.of(agent));

        List<Agent> agents = agentService.getAllAgents();

        assertThat(agents).hasSize(1);
        assertThat(agents.get(0)).isEqualTo(agent);
    }

    @Test
    void getAgentById_ShouldReturnAgentInfo_WhenFound() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

        List<String> result = agentService.getAgentById(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(agent.getUsername());
    }

    @Test
    void getAgentById_ShouldReturnNull_WhenNotFound() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        List<String> result = agentService.getAgentById(1L);

        assertThat(result).isNull();
    }

    @Test
    void getAgentByOidcSub_ShouldReturnAgent_WhenFound() {
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);

        Agent result = agentService.getAgentByOidcSub("agent123");

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo("agent123");
    }

    @Test
    void getAgentByOidcSub_ShouldReturnNull_WhenNotFound() {
        when(agentRepository.findByOidcSub("agent123")).thenReturn(null);

        Agent result = agentService.getAgentByOidcSub("agent123");

        assertThat(result).isNull();
    }

    @Test
    void updateAgent_ShouldReturnUpdatedAgent() {
        agent.setUsername("newAgent");
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        Agent result = agentService.updateAgent(agent);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newAgent");
    }

    @Test
    void deleteAgentById_ShouldCallDeleteMethod() {
        doNothing().when(agentRepository).deleteById(1L);

        agentService.deleteAgentById(1L);

        verify(agentRepository).deleteById(1L);
    }

    @Test
    void isRegistered_ShouldReturnTrue_WhenAgentExists() {
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);

        boolean result = agentService.isRegistered("agent123");

        assertThat(result).isTrue();
    }

    @Test
    void isRegistered_ShouldReturnFalse_WhenAgentDoesNotExist() {
        when(agentRepository.findByOidcSub("agent123")).thenReturn(null);

        boolean result = agentService.isRegistered("agent123");

        assertThat(result).isFalse();
    }

    @Test
    void reassignToAgent_ShouldReturnNewAgent() {
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        Agent result = agentService.reassignToAgent(tenant);

        assertThat(result).isNotNull();
        assertThat(result.getOidcSub()).isEqualTo(agent.getOidcSub());
        assertThat(result.getRole()).isEqualTo(Role.AGENT);
    }

    @Test
    void updatePreference_ShouldReturnUpdatedAgent() {
        AgentPrefUpdateDto updateDto = new AgentPrefUpdateDto("agent123", true);
        when(agentRepository.findByOidcSub("agent123")).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        Agent result = agentService.updatePreference(updateDto);

        assertThat(result).isNotNull();
        assertThat(result.isVerified()).isTrue();
    }
}
