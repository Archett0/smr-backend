package com.team12.userservice;

import com.team12.userservice.model.Role;
import com.team12.userservice.util.UserServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUtilTest {

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRoleFromJwt_ShouldThrowException_WhenRolesAreNull() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> UserServiceUtil.getRoleFromJwt(jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User has no role assigned");
    }

    @Test
    void getRoleFromJwt_ShouldThrowException_WhenRolesAreEmpty() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> UserServiceUtil.getRoleFromJwt(jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User has no role assigned");
    }

    @Test
    void getRoleFromJwt_ShouldReturnRole_WhenRoleIsValid() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("ADMIN"));

        // Act
        Role role = UserServiceUtil.getRoleFromJwt(jwt);

        // Assert
        assertThat(role).isEqualTo(Role.ADMIN);
    }

    @Test
    void getRoleFromJwt_ShouldThrowException_WhenRoleIsInvalid() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("INVALID_ROLE"));

        // Act & Assert
        assertThatThrownBy(() -> UserServiceUtil.getRoleFromJwt(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No enum constant com.team12.userservice.model.Role.INVALID_ROLE");
    }

    @Test
    void getOidcSubFromJwt_ShouldReturnOidcSub() {
        // Arrange
        when(jwt.getSubject()).thenReturn("sub123");

        // Act
        String oidcSub = UserServiceUtil.getOidcSubFromJwt(jwt);

        // Assert
        assertThat(oidcSub).isEqualTo("sub123");
    }

    @Test
    void getRoleFromJwt_ShouldReturnRole_WhenMultipleRolesExist() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of("TENANT", "ADMIN"));

        // Act
        Role role = UserServiceUtil.getRoleFromJwt(jwt);

        // Assert
        assertThat(role).isEqualTo(Role.TENANT);  // Assuming the first role is returned, adjust accordingly
    }

    @Test
    void getRoleFromJwt_ShouldThrowException_WhenEmptyRoleString() {
        // Arrange
        when(jwt.getClaimAsStringList("https://smr.com/roles")).thenReturn(List.of(""));

        // Act & Assert
        assertThatThrownBy(() -> UserServiceUtil.getRoleFromJwt(jwt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_ShouldThrowException_WhenInstantiated() throws Exception {
        // Use reflection to access the private constructor
        Constructor<UserServiceUtil> constructor = UserServiceUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        // Try to instantiate the utility class using reflection (which should throw an exception)
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class);
    }
}
