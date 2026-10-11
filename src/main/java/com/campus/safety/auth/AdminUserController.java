package com.campus.safety.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserView> listUsers() {
        return users.findAll().stream().map(UserView::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserView createUser(@Valid @RequestBody CreateUserRequest request) {
        if (users.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        AppUser user = users.save(new AppUser(
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                request.role()));
        return UserView.from(user);
    }

    @PutMapping("/{id}")
    @Transactional
    public UserView updateUser(@PathVariable long id,
                               @Valid @RequestBody UpdateUserRequest request,
                               Authentication authentication) {
        AppUser user = findUser(id);
        boolean removingAdmin = user.getRole() == AppRole.ADMIN
                && (request.role() != AppRole.ADMIN || !request.enabled());
        if (removingAdmin && users.countByRoleAndEnabledTrue(AppRole.ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "At least one enabled administrator must remain");
        }
        if (!request.enabled() && user.getUsername().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot disable your own account");
        }
        if (user.getUsername().equalsIgnoreCase(authentication.getName())
                && user.getRole() == AppRole.ADMIN && request.role() != AppRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot change your own administrator role");
        }
        user.changeRole(request.role());
        user.setEnabled(request.enabled());
        return UserView.from(users.save(user));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteUser(@PathVariable long id, Authentication authentication) {
        AppUser user = findUser(id);
        if (user.getUsername().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot delete your own account");
        }
        if (user.getRole() == AppRole.ADMIN && user.isEnabled()
                && users.countByRoleAndEnabledTrue(AppRole.ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "At least one enabled administrator must remain");
        }
        users.delete(user);
    }

    private AppUser findUser(long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String username,
            @NotBlank @Size(min = 12, max = 100) String password,
            @NotNull AppRole role) {
    }

    public record UpdateUserRequest(@NotNull AppRole role, boolean enabled) {
    }

    public record UserView(Long id, String username, AppRole role, boolean enabled) {

        static UserView from(AppUser user) {
            return new UserView(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
        }
    }
}
