package com.team12.userservice.dto;

import com.team12.userservice.model.BaseUser;
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

    public LoginCompleteDto(BaseUser user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.id = user.getId();
        this.oidcSub = user.getOidcSub();
    }
}
