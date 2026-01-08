package com.cristian.backend.service;

import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import com.cristian.backend.security.OAuthUser;
import com.cristian.backend.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateGoogleUser(OAuthUser oauthUser) {

        return userRepository.findByEmail(oauthUser.getEmail())
                .orElseGet(() -> {
                    User user = new User();
                    //random username generation
                    user.setUsername(oauthUser.getFirstName().toLowerCase() + "."
                            + oauthUser.getLastName().toLowerCase() + System.currentTimeMillis() % 1000 );
                    user.setEmail(oauthUser.getEmail());
                    user.setFirstName(oauthUser.getFirstName());
                    user.setLastName(oauthUser.getLastName());
                    user.setPassword(null);
                    user.setProvider(User.AuthProvider.GOOGLE);
                    user.setEnabled(true);
                    return userRepository.save(user);
                });
    }

    public User changeUserRole(String email, User.Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User changeUserRoleById(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
