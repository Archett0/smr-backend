package com.team12.userservice.util;

import com.team12.userservice.model.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class UserServiceUtil {
    /**
     * Retrieve roles from access token
     *
     * @param jwt access token
     * @return Role role
     */
    public static Role getRoleFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("https://smr.com/roles");
        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("User has no role assigned");
        }
        String roleStr = roles.getFirst();
        return Role.valueOf(roleStr);
    }

    /**
     * Retrieve subject from access token
     *
     * @param jwt access token
     * @return String sub
     */
    public static String getOidcSubFromJwt(Jwt jwt) {
        return jwt.getSubject();
    }
}
