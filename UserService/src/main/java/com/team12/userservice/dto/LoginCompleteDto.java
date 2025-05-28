package com.team12.userservice.dto;

import com.team12.userservice.model.Admin;
import com.team12.userservice.model.Agent;
import com.team12.userservice.model.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LoginCompleteDto {
    private String username;
    private String email;
    private String picture;
    private Long id;
    private String oidcSub;

    public LoginCompleteDto(Tenant tenant) {
        this.username = tenant.getUsername();
        this.email = tenant.getEmail();
        this.picture = tenant.getPicture();
        this.id = tenant.getId();
        this.oidcSub = tenant.getOidcSub();
    }

    public LoginCompleteDto(Admin admin) {
        this.username = admin.getUsername();
        this.email = admin.getEmail();
        this.picture = admin.getPicture();
        this.id = admin.getId();
        this.oidcSub = admin.getOidcSub();
    }

    public LoginCompleteDto(Agent agent) {
        this.username = agent.getUsername();
        this.email = agent.getEmail();
        this.picture = agent.getPicture();
        this.id = agent.getId();
        this.oidcSub = agent.getOidcSub();
    }
}
