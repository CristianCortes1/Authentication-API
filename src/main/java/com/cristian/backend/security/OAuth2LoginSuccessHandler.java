package com.cristian.backend.security;

import com.cristian.backend.model.User;
import com.cristian.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    // viene de application-dev.properties / application-prod.properties
    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        OAuthUser oauthUser = new OAuthUser(principal);

        User user = userService.findOrCreateGoogleUser(oauthUser);

        // Generar JWT con el rol del usuario
        String jwt = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 🍪 Crear cookie segura
        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)          // JS no puede leerla (protege XSS)
                .secure(cookieSecure)    // false en dev, true en prod
                .path("/")               // disponible en toda la app
                .sameSite("Lax")         // compatible con OAuth
                .build();

        // Enviar cookie al navegador
        response.addHeader("Set-Cookie", cookie.toString());

        // Redirigir al frontend (ya autenticado)
        response.sendRedirect("http://localhost:8080/swagger-ui.html");
    }
}

