package com.cristian.backend.security;

import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Si ya hay autenticación, continuar
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Buscar token en cookies
        String token = extractTokenFromCookie(request);

        if (token != null && !token.isEmpty()) {
            try {
                // Validar que el token no haya expirado
                if (!jwtService.isTokenExpired(token)) {
                    String email = jwtService.extractSubject(token);
                    String role = jwtService.extractRole(token);

                    log.info("Token encontrado para: {} con rol en token: {}", email, role);

                    // Si el token no tiene rol, buscamos en la base de datos
                    if (role == null || role.isEmpty()) {
                        Optional<User> userOpt = userRepository.findByEmail(email);
                        if (userOpt.isPresent()) {
                            role = userOpt.get().getRole().name();
                            log.info("Rol obtenido de BD para {}: {}", email, role);
                        } else {
                            role = "USER"; // default
                            log.warn("Usuario no encontrado en BD, usando rol por defecto: USER");
                        }
                    }

                    if (email != null) {
                        // Crear autoridad con el prefijo ROLE_
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        Collections.singletonList(authority)
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info(" Usuario autenticado: {} con rol: ROLE_{}", email, role);
                    }
                } else {
                    log.warn("Token expirado");
                }
            } catch (Exception e) {
                log.error(" Error al procesar token de cookie: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

