package com.supplychain.scfapp.security;

import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil,
                         UserDetailsService userDetailsService,
                         UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    // ✅ ne pas filtrer les endpoints publics d'auth (login, refresh, reset, logout)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getServletPath();
        return p.equals("/auth/login")
                || p.equals("/auth/refresh")
                || p.equals("/auth/request-password-reset")
                || p.equals("/auth/reset-password")
                || p.equals("/auth/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // 1) Signature/expiration du JWT
        if (!jwtUtil.isTokenValid(token)) {
            unauthorized(response, "Invalid or expired token");
            return;
        }

        String username = jwtUtil.extractUsername(token);
        Instant issuedAt = jwtUtil.getIssuedAt(token);

        // 2) Utilisateur existe ?
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            unauthorized(response, "User not found for token");
            return;
        }

        User user = userOpt.get();

        // 3) Invalidation par changement de mot de passe
        Instant pwdChangedAt = user.getPasswordChangedAt();
        if (pwdChangedAt != null && issuedAt != null && issuedAt.isBefore(pwdChangedAt)) {
            unauthorized(response, "Token invalidated (password changed)");
            return;
        }

        // 4) OK -> authentifier la requête
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
