package com.campus.safety.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AdminUserControllerTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AdminUserController controller = new AdminUserController(users, passwordEncoder);

    @Test
    void storesNewAccountPasswordsAsHashes() {
        String password = "a-strong-password";
        when(users.existsByUsernameIgnoreCase("operator.one")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("bcrypt-hash");
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserController.UserView created = controller.createUser(
                new AdminUserController.CreateUserRequest("operator.one", password, AppRole.OPERATOR));

        assertThat(created.username()).isEqualTo("operator.one");
        assertThat(created.role()).isEqualTo(AppRole.OPERATOR);
        verify(users).save(org.mockito.ArgumentMatchers.argThat(
                account -> account.getPassword().equals("bcrypt-hash")));
    }

    @Test
    void refusesToDeleteTheLastEnabledAdministrator() {
        AppUser administrator = new AppUser("admin", "encoded", AppRole.ADMIN);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("another-admin");
        when(users.findById(1L)).thenReturn(Optional.of(administrator));
        when(users.countByRoleAndEnabledTrue(AppRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> controller.deleteUser(1L, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
