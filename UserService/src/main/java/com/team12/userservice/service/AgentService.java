package com.team12.userservice.service;

import com.team12.userservice.dto.AgentPrefUpdateDto;
import com.team12.userservice.model.Agent;
import com.team12.userservice.model.Role;
import com.team12.userservice.model.Tenant;
import com.team12.userservice.repository.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService {
    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public Agent addAgent(Agent agent) {
        return agentRepository.save(agent);
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<String> getAgentById(Long id) {
        return agentRepository.findById(id)
                .map(agent -> List.of(
                        agent.getUsername(),
                        agent.getPhoneNumber() == null ? "" : agent.getPhoneNumber()
                ))
                .orElse(null);
    }

    public Agent getAgentByOidcSub(String oidcSub) {
        return agentRepository.findByOidcSub(oidcSub);
    }

    public Agent updateAgent(Agent agent) {
        return agentRepository.save(agent);
    }

    public void deleteAgentById(Long id) {
        agentRepository.deleteById(id);
    }

    public boolean isRegistered(String oidcSub) {
        return agentRepository.findByOidcSub(oidcSub) != null;
    }

    public Agent reassignToAgent(Tenant tenant) {
        Agent newAgent = new Agent();
        newAgent.setOidcSub(tenant.getOidcSub());
        newAgent.setVerified(false);
        newAgent.setUsername(tenant.getUsername());
        newAgent.setEmail(tenant.getEmail());
        newAgent.setRegisteredAt(tenant.getRegisteredAt());
        newAgent.setLastLoginAt(tenant.getLastLoginAt());
        newAgent.setEnabled(true);
        newAgent.setPicture(tenant.getPicture());
        newAgent.setRole(Role.AGENT);
        return agentRepository.save(newAgent);
    }

    public Agent updatePreference(AgentPrefUpdateDto dto) {
        Agent agent = agentRepository.findByOidcSub(dto.getOidcSub());
        agent.setVerified(dto.isVerified());
        agentRepository.save(agent);
        return agent;
    }
}
