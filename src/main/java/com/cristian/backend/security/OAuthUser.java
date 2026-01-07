package com.cristian.backend.security;

import lombok.Getter;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class OAuthUser {

    private final String email;
    private final String firstName;
    private final String lastName;

    public OAuthUser(OAuth2User user) {
        this.email = user.getAttribute("email");
        this.firstName = user.getAttribute("given_name");
        this.lastName = user.getAttribute("family_name");
    }

}
