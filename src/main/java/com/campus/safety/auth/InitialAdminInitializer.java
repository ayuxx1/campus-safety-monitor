package com.campus.safety.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public InitialAdminInitializer(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${campus.admin-username:admin}") String username,
            @Value("${campus.admin-password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() != 0) {
            return;
        }
        if (password.length() < 12) {
            throw new IllegalStateException(
                    "Set CAMPUS_ADMIN_PASSWORD to a password of at least 12 characters to create the initial admin.");
        }
        if (username.isBlank() || username.length() > 80) {
            throw new IllegalStateException("CAMPUS_ADMIN_USERNAME must contain 1 to 80 characters.");
        }
        users.save(new AppUser(username.trim(), passwordEncoder.encode(password), AppRole.ADMIN));
    }
}
