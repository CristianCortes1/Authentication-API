package com.cristian.backend.security;

import com.cristian.backend.model.User;
import com.cristian.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        OAuthUser oauthUser = new OAuthUser(principal);

        User user = userService.findOrCreateGoogleUser(oauthUser);

        log.info("OAuth2 login successful for user: {} with role: {}", user.getEmail(), user.getRole());

        // Generate JWT with user role
        String jwt = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 🍪 Create secure cookie
        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(86400)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Redirect to frontend
        String redirectUrl = frontendUrl + "/auth/callback";
        log.info("Redirecting to: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}

