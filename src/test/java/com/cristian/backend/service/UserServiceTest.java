package com.cristian.backend.service;

import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import com.cristian.backend.security.OAuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private OAuthUser oauthUser;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Crear mock de OAuth2User
        OAuth2User oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("john.doe@google.com");
        when(oauth2User.getAttribute("given_name")).thenReturn("John");
        when(oauth2User.getAttribute("family_name")).thenReturn("Doe");

        oauthUser = new OAuthUser(oauth2User);

        existingUser = User.builder()
                .id(1L)
                .username("john.doe")
                .email("john.doe@google.com")
                .firstName("John")
                .lastName("Doe")
                .password(null)
                .enabled(true)
                .provider(User.AuthProvider.GOOGLE)
                .build();
    }

    // ============ CASOS DE ÉXITO ============

    @Test
    @DisplayName("Should return existing user when already registered in the database")
    void findOrCreateGoogleUser_UserExists() {
        // GIVEN
        when(userRepository.findByEmail("john.doe@google.com"))
                .thenReturn(Optional.of(existingUser));

        // WHEN
        User result = userService.findOrCreateGoogleUser(oauthUser);

        // THEN
        assertNotNull(result);
        assertEquals("john.doe@google.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(User.AuthProvider.GOOGLE, result.getProvider());
        assertTrue(result.getEnabled());
        assertNull(result.getPassword());
        verify(userRepository, times(1)).findByEmail("john.doe@google.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create a new user when it does not exist in the database")
    void findOrCreateGoogleUser_UserDoesNotExist_CreatesNewUser() {
        // GIVEN
        when(userRepository.findByEmail("john.doe@google.com"))
                .thenReturn(Optional.empty());

        User newUser = User.builder()
                .id(2L)
                .username("john.doe")
                .email("john.doe@google.com")
                .firstName("John")
                .lastName("Doe")
                .password(null)
                .enabled(true)
                .provider(User.AuthProvider.GOOGLE)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(newUser);

        // WHEN
        User result = userService.findOrCreateGoogleUser(oauthUser);

        // THEN
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("john.doe@google.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(User.AuthProvider.GOOGLE, result.getProvider());
        assertTrue(result.getEnabled());
        assertNull(result.getPassword());
        verify(userRepository, times(1)).findByEmail("john.doe@google.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should generate username with correct format: firstName.lastName{timestamp}")
    void findOrCreateGoogleUser_NewUser_HasCorrectUsernameFormat() {
        // GIVEN
        when(userRepository.findByEmail("john.doe@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(3L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(oauthUser);

        // THEN
        assertNotNull(result);
        // El username debe tener el formato: firstName.lastName{timestamp}
        assertTrue(result.getUsername().startsWith("john.doe"));
        // El timestamp puede ser de 1 a 3 dígitos (0-999)
        assertTrue(result.getUsername().matches("john\\.doe\\d+"));
    }

    @Test
    @DisplayName("Should create a new user with null password for OAuth users")
    void findOrCreateGoogleUser_NewUser_PasswordIsNull() {
        // GIVEN
        OAuth2User jane = mock(OAuth2User.class);
        when(jane.getAttribute("email")).thenReturn("jane.smith@google.com");
        when(jane.getAttribute("given_name")).thenReturn("Jane");
        when(jane.getAttribute("family_name")).thenReturn("Smith");
        OAuthUser janeUser = new OAuthUser(jane);

        when(userRepository.findByEmail("jane.smith@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(4L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(janeUser);

        // THEN
        assertNull(result.getPassword());
    }

    @Test
    @DisplayName("Should create a new user with enabled status by default")
    void findOrCreateGoogleUser_NewUser_EnabledByDefault() {
        // GIVEN
        OAuth2User test = mock(OAuth2User.class);
        when(test.getAttribute("email")).thenReturn("test@google.com");
        when(test.getAttribute("given_name")).thenReturn("Test");
        when(test.getAttribute("family_name")).thenReturn("User");
        OAuthUser testUser = new OAuthUser(test);

        when(userRepository.findByEmail("test@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(5L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(testUser);

        // THEN
        assertTrue(result.getEnabled());
    }

    @Test
    @DisplayName("Should create a new user with OAuth provider set to Google")
    void findOrCreateGoogleUser_NewUser_ProviderIsGoogle() {
        // GIVEN
        OAuth2User google = mock(OAuth2User.class);
        when(google.getAttribute("email")).thenReturn("google.user@google.com");
        when(google.getAttribute("given_name")).thenReturn("Google");
        when(google.getAttribute("family_name")).thenReturn("User");
        OAuthUser googleUser = new OAuthUser(google);

        when(userRepository.findByEmail("google.user@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(6L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(googleUser);

        // THEN
        assertEquals(User.AuthProvider.GOOGLE, result.getProvider());
    }

    // ============ CASOS DE FALLO ============

    @Test
    @DisplayName("Should throw exception when repository fails to find the user")
    void findOrCreateGoogleUser_RepositoryThrowsException_WhenFindingUser() {
        // GIVEN
        OAuth2User error = mock(OAuth2User.class);
        when(error.getAttribute("email")).thenReturn("error@google.com");
        when(error.getAttribute("given_name")).thenReturn("Error");
        when(error.getAttribute("family_name")).thenReturn("User");
        OAuthUser errorUser = new OAuthUser(error);

        when(userRepository.findByEmail("error@google.com"))
                .thenThrow(new RuntimeException("Database connection error"));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> userService.findOrCreateGoogleUser(errorUser));
        verify(userRepository, times(1)).findByEmail("error@google.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when repository fails to save the user")
    void findOrCreateGoogleUser_RepositoryThrowsException_WhenSavingUser() {
        // GIVEN
        OAuth2User saveError = mock(OAuth2User.class);
        when(saveError.getAttribute("email")).thenReturn("save.error@google.com");
        when(saveError.getAttribute("given_name")).thenReturn("Save");
        when(saveError.getAttribute("family_name")).thenReturn("Error");
        OAuthUser saveErrorUser = new OAuthUser(saveError);

        when(userRepository.findByEmail("save.error@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Failed to save user"));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> userService.findOrCreateGoogleUser(saveErrorUser));
        verify(userRepository, times(1)).findByEmail("save.error@google.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void findOrCreateGoogleUser_WithNullEmail() {
        // GIVEN
        OAuth2User nullEmail = mock(OAuth2User.class);
        when(nullEmail.getAttribute("email")).thenReturn(null);
        when(nullEmail.getAttribute("given_name")).thenReturn("Null");
        when(nullEmail.getAttribute("family_name")).thenReturn("Email");
        OAuthUser nullEmailUser = new OAuthUser(nullEmail);

        when(userRepository.findByEmail(null))
                .thenThrow(new IllegalArgumentException("Email cannot be null"));

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> userService.findOrCreateGoogleUser(nullEmailUser));
    }

    @Test
    @DisplayName("Should create user correctly when name contains special characters")
    void findOrCreateGoogleUser_WithSpecialCharactersInName() {
        // GIVEN
        OAuth2User special = mock(OAuth2User.class);
        when(special.getAttribute("email")).thenReturn("special@google.com");
        when(special.getAttribute("given_name")).thenReturn("José");
        when(special.getAttribute("family_name")).thenReturn("García");
        OAuthUser specialCharUser = new OAuthUser(special);

        when(userRepository.findByEmail("special@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(7L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(specialCharUser);

        // THEN
        assertNotNull(result);
        assertEquals("special@google.com", result.getEmail());
        assertEquals("José", result.getFirstName());
        assertEquals("García", result.getLastName());
    }

    @Test
    @DisplayName("Should return the same user on multiple calls with the same email")
    void findOrCreateGoogleUser_MultipleCallsSameEmail_ReturnsSameUser() {
        // GIVEN
        when(userRepository.findByEmail("same@google.com"))
                .thenReturn(Optional.of(existingUser));

        OAuth2User user1OAuth = mock(OAuth2User.class);
        when(user1OAuth.getAttribute("email")).thenReturn("same@google.com");
        when(user1OAuth.getAttribute("given_name")).thenReturn("Same");
        when(user1OAuth.getAttribute("family_name")).thenReturn("User");
        OAuthUser user1 = new OAuthUser(user1OAuth);

        OAuth2User user2OAuth = mock(OAuth2User.class);
        when(user2OAuth.getAttribute("email")).thenReturn("same@google.com");
        when(user2OAuth.getAttribute("given_name")).thenReturn("Same");
        when(user2OAuth.getAttribute("family_name")).thenReturn("User");
        OAuthUser user2 = new OAuthUser(user2OAuth);

        // WHEN
        User result1 = userService.findOrCreateGoogleUser(user1);
        User result2 = userService.findOrCreateGoogleUser(user2);

        // THEN
        assertEquals(result1.getId(), result2.getId());
        assertEquals(result1.getEmail(), result2.getEmail());
        verify(userRepository, times(2)).findByEmail("same@google.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create user correctly when first name is empty")
    void findOrCreateGoogleUser_EmptyFirstName() {
        // GIVEN
        OAuth2User empty = mock(OAuth2User.class);
        when(empty.getAttribute("email")).thenReturn("empty@google.com");
        when(empty.getAttribute("given_name")).thenReturn("");
        when(empty.getAttribute("family_name")).thenReturn("User");
        OAuthUser emptyFirstNameUser = new OAuthUser(empty);

        when(userRepository.findByEmail("empty@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(8L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(emptyFirstNameUser);

        // THEN
        assertNotNull(result);
        // El username debe ser: .user{timestamp}
        assertTrue(result.getUsername().startsWith(".user"));
        // El timestamp puede ser de 1 a 3 dígitos (0-999)
        assertTrue(result.getUsername().matches("\\.user\\d+"));
    }

    @Test
    @DisplayName("Should create user correctly when last name is empty")
    void findOrCreateGoogleUser_EmptyLastName() {
        // GIVEN
        OAuth2User emptyLast = mock(OAuth2User.class);
        when(emptyLast.getAttribute("email")).thenReturn("empty.last@google.com");
        when(emptyLast.getAttribute("given_name")).thenReturn("First");
        when(emptyLast.getAttribute("family_name")).thenReturn("");
        OAuthUser emptyLastNameUser = new OAuthUser(emptyLast);

        when(userRepository.findByEmail("empty.last@google.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(9L);
                    return user;
                });

        // WHEN
        User result = userService.findOrCreateGoogleUser(emptyLastNameUser);

        // THEN
        assertNotNull(result);
        // El username debe contener un punto y números (timestamp)
        String username = result.getUsername();
        assertTrue(username.contains("."), "El username debe contener un punto");
        // Extrae todos los números del username
        String numericPart = username.replaceAll("\\D", "");
        assertFalse(numericPart.isEmpty(), "El username debe contener números del timestamp");
    }

    @Test
    @DisplayName("Should create user correctly when email is very long")
    void findOrCreateGoogleUser_LongEmail() {
        // GIVEN
        String longEmail = "very.long.email.address.with.many.characters@very.long.domain.google.com";
        OAuth2User longEmailOAuth = mock(OAuth2User.class);
        when(longEmailOAuth.getAttribute("email")).thenReturn(longEmail);
        when(longEmailOAuth.getAttribute("given_name")).thenReturn("Long");
        when(longEmailOAuth.getAttribute("family_name")).thenReturn("Email");
        OAuthUser longEmailUser = new OAuthUser(longEmailOAuth);

        when(userRepository.findByEmail(longEmail))
                .thenReturn(Optional.empty());

        User newUser = User.builder()
                .id(10L)
                .email(longEmail)
                .firstName("Long")
                .lastName("Email")
                .provider(User.AuthProvider.GOOGLE)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(newUser);

        // WHEN
        User result = userService.findOrCreateGoogleUser(longEmailUser);

        // THEN
        assertNotNull(result);
        assertEquals(longEmail, result.getEmail());
    }

}

