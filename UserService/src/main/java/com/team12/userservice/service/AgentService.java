package com.team12.userservice.service;

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

    public Agent getAgentById(Long id) {
        return agentRepository.findById(id).orElse(null);
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
}
