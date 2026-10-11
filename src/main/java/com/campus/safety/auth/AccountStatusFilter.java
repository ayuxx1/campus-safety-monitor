package com.campus.safety.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

final class AccountStatusFilter extends OncePerRequestFilter {

    private final AppUserRepository users;
    private final SecurityContextRepository contextRepository =
            new HttpSessionSecurityContextRepository();

    AccountStatusFilter(AppUserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            AppUser account = users.findByUsernameIgnoreCase(authentication.getName()).orElse(null);
            if (account == null || !account.isEnabled()) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Account is disabled or unavailable");
                return;
            }
            var refreshedAuthentication = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(refreshedAuthentication);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);
        }
        filterChain.doFilter(request, response);
    }
}
