package com.cristian.backend.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuthUser {

    private final String email;
    private final String firstName;
    private final String lastName;

    public OAuthUser(OAuth2User user) {
        this.email = user.getAttribute("email");
        this.firstName = user.getAttribute("given_name");
        this.lastName = user.getAttribute("family_name");
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
