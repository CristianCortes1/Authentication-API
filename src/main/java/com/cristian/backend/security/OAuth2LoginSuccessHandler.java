package com.cristian.backend.security;

import com.cristian.backend.model.User;
import com.cristian.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        OAuthUser oauthUser = new OAuthUser(principal);

        User user = userService.findOrCreateGoogleUser(oauthUser);

        String jwt = jwtService.generateToken(user.getUsername());

        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "token": "%s"
            }
        """.formatted(jwt));

        response.getWriter().flush();
    }
}
