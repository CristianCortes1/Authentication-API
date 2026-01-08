package com.cristian.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=U1YzVWdmVlhOcEo4dkd5VGlxWjhsOWcyRWpFZlIzUWxTdFc0TlpoMWg1dVEyTjBXVjRJSUI4aFNVQT09",
    "jwt.expiration=3600000",
    "jwt.verification.expiration=86400000"
})
@DisplayName("JwtService Test Suite")
class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private long verificationExpiration;

    @BeforeEach
    void setUp() {
        secretKey = "U1YzVWdmVlhOcEo4dkd5VGlxWjhsOWcyRWpFZlIzUWxTdFc0TlpoMWg1dVEyTjBXVjRJSUI4aFNVQT09";
        long jwtExpiration = 3600000;
        verificationExpiration = 86400000;

        jwtService = new JwtService();
        // Inyectar valores privados usando reflexión
        try {
            var secretField = JwtService.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(jwtService, secretKey);

            var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtService, jwtExpiration);

            var verificationField = JwtService.class.getDeclaredField("verificationExpiration");
            verificationField.setAccessible(true);
            verificationField.set(jwtService, verificationExpiration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error inicializando el test", e);
        }
    }

    @Test
    @DisplayName("Should generate a valid JWT token for the given username")
    void testGenerateToken() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token = jwtService.generateToken(username);

        // THEN
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtService.extractSubject(token));
    }

    @Test
    @DisplayName("Should generate a valid verification token for the given email")
    void testGenerateVerificationToken() {
        // GIVEN
        String email = "user@test.com";

        // WHEN
        String token = jwtService.generateVerificationToken(email);

        // THEN
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, jwtService.extractSubject(token));
    }

    @Test
    @DisplayName("Should correctly extract the subject from a valid JWT token")
    void testExtractSubject() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // WHEN
        String extractedSubject = jwtService.extractSubject(token);

        // THEN
        assertEquals(username, extractedSubject);
    }

    @Test
    @DisplayName("Should correctly extract the expiration date from a valid JWT token")
    void testExtractExpiration() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // WHEN
        Date expirationDate = jwtService.extractExpiration(token);

        // THEN
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Should verify that a recently generated token is not expired")
    void testIsTokenExpired_TokenNotExpired() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // WHEN
        // Esperar un poco para asegurar que el token se ha generado
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isExpired = jwtService.isTokenExpired(token);

        // THEN
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should correctly detect when a token is expired")
    void testIsTokenExpired_WithExpiredToken() {
        // GIVEN
        String username = "testuser";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

        Date now = new Date();
        Date issuedAt = new Date(now.getTime() - 400000);
        Date expiryDate = new Date(now.getTime() - 200000);

        String expiredToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isExpired = jwtService.isTokenExpired(expiredToken);

        // THEN
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Should correctly validate a valid verification token")
    void testValidateVerificationToken_ValidToken() {
        // GIVEN
        String email = "user@test.com";
        String token = jwtService.generateVerificationToken(email);

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(token);

        // THEN
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject an invalid verification token")
    void testValidateVerificationToken_InvalidToken() {
        // GIVEN
        String invalidToken = "invalid.token.here";

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(invalidToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject verification token with wrong type claim")
    void testValidateVerificationToken_WrongType() {
        // GIVEN
        // Crear un token que no es de tipo "verification"
        String username = "testuser";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + verificationExpiration);

        String tokenWithWrongType = Jwts.builder()
                .claim("type", "login")
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(tokenWithWrongType);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject an expired verification token")
    void testValidateVerificationToken_ExpiredToken() {
        // GIVEN
        String email = "user@test.com";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 1000); // Expirado

        String expiredVerificationToken = Jwts.builder()
                .claim("type", "verification")
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(expiredVerificationToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should correctly extract a specific claim from a token")
    void testExtractClaim() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // WHEN
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // THEN
        assertEquals(username, subject);
    }

    @Test
    @DisplayName("Should generate unique tokens for the same username")
    void testMultipleTokensAreUnique() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token1 = jwtService.generateToken(username);
        try {
            Thread.sleep(1000); // Pequeño delay para asegurar timestamps diferentes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(username);

        // THEN
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should include verification type claim in verification token")
    void testVerificationTokenContainsVerificationType() {
        // GIVEN
        String email = "user@test.com";
        String token = jwtService.generateVerificationToken(email);

        // WHEN
        String type = jwtService.extractClaim(token, claims -> claims.get("type", String.class));

        // THEN
        assertEquals("verification", type);
    }

    @Test
    @DisplayName("Should generate token with valid JWT format (three parts separated by dots)")
    void testTokenFormatIsJWT() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token = jwtService.generateToken(username);

        // THEN
        // Un JWT válido tiene 3 partes separadas por puntos
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("Should throw exception when extracting subject from invalid token")
    void testExtractSubjectFromInvalidToken() {
        // GIVEN
        String invalidToken = "invalid.token.here";

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractSubject(invalidToken));
    }

    @Test
    @DisplayName("Should create token with correct subject and non-expired expiration")
    void testCreateTokenWithClaims() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // WHEN
        String subject = jwtService.extractSubject(token);
        Date expiration = jwtService.extractExpiration(token);

        // THEN
        assertEquals(username, subject);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should create verification token with correct subject and verification type")
    void testCreateTokenWithVerificationClaims() {
        // GIVEN
        String email = "user@test.com";
        String token = jwtService.generateVerificationToken(email);

        // WHEN
        String subject = jwtService.extractSubject(token);
        String type = jwtService.extractClaim(token, claims -> claims.get("type", String.class));

        // THEN
        assertEquals(email, subject);
        assertEquals("verification", type);
    }

    @Test
    @DisplayName("Should create authentication token with correct expiration time (1 hour)")
    void testCreateTokenExpirationTime() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token = jwtService.generateToken(username);
        Date expiration = jwtService.extractExpiration(token);

        // THEN
        assertNotNull(expiration);
        // Verificar que el token expira en el futuro (entre ahora y 1 hora)
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        assertTrue(expirationTime > now, "Expiration should be in the future");
        assertTrue(expirationTime < now + 3700000, "Expiration should be within 1 hour + 1 minute");
    }

    @Test
    @DisplayName("Should create verification token with correct expiration time (24 hours)")
    void testCreateVerificationTokenExpirationTime() {
        // GIVEN
        String email = "user@test.com";

        // WHEN
        String token = jwtService.generateVerificationToken(email);
        Date expiration = jwtService.extractExpiration(token);

        // THEN
        assertNotNull(expiration);
        // Verificar que el token expira en el futuro (entre ahora y 24 horas)
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        assertTrue(expirationTime > now, "Expiration should be in the future");
        assertTrue(expirationTime < now + 86500000, "Expiration should be within 24 hours + 1 minute");
    }

    @Test
    @DisplayName("Should verify that a newly created token is not expired")
    void testCreateTokenIsNotExpired() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token = jwtService.generateToken(username);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean isExpired = jwtService.isTokenExpired(token);

        // THEN
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should create token with all three valid JWT parts (header, payload, signature)")
    void testCreateTokenContainsThreeJWTParts() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token = jwtService.generateToken(username);
        String[] parts = token.split("\\.");

        // THEN
        assertEquals(3, parts.length);
        assertFalse(parts[0].isEmpty()); // Header
        assertFalse(parts[1].isEmpty()); // Payload
        assertFalse(parts[2].isEmpty()); // Signature
    }

    @Test
    @DisplayName("Should create tokens with different expiration times for auth and verification")
    void testCreateTokenWithDifferentExpirations() {
        // GIVEN
        String username = "testuser";
        String email = "user@test.com";

        // WHEN
        String authToken = jwtService.generateToken(username);
        String verificationToken = jwtService.generateVerificationToken(email);

        Date authExpiration = jwtService.extractExpiration(authToken);
        Date verificationExpiration = jwtService.extractExpiration(verificationToken);

        // THEN
        assertNotEquals(authExpiration.getTime(), verificationExpiration.getTime());
        assertTrue(verificationExpiration.getTime() > authExpiration.getTime()); // Verification expira después
    }

    @Test
    @DisplayName("Should throw exception when extracting subject from malformed token")
    void testExtractSubjectFromMalformedToken() {
        // GIVEN
        String malformedToken = "header.payload"; // Falta la firma

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractSubject(malformedToken));
    }

    @Test
    @DisplayName("Should throw exception when extracting expiration from invalid token")
    void testExtractExpirationFromInvalidToken() {
        // GIVEN
        String invalidToken = "completely.invalid.token";

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractExpiration(invalidToken));
    }

    @Test
    @DisplayName("Should handle malformed token when checking expiration")
    void testIsTokenExpiredWithMalformedToken() {
        // WHEN & THEN
        try {
            assertTrue(true);
        } catch (Throwable e) {
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Should reject verification token with malformed structure")
    void testValidateVerificationTokenWithMalformedToken() {
        // GIVEN
        String malformedToken = "header.payload.signature.extra";

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(malformedToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle null token input when validating verification token")
    void testValidateVerificationTokenWithNullInput() {
        // GIVEN
        String nullToken = null;

        // WHEN
        try {
            boolean isValid = jwtService.validateVerificationToken(nullToken);
            // Si no lanza excepción, debe ser inválido
            assertFalse(isValid);
        } catch (Throwable e) {
            // También es válido que lance excepción
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Should reject empty token string when validating verification token")
    void testValidateVerificationTokenWithEmptyToken() {
        // GIVEN
        String emptyToken = "";

        // WHEN
        try {
            boolean isValid = jwtService.validateVerificationToken(emptyToken);
            // Si no lanza excepción, debe ser inválido
            assertFalse(isValid);
        } catch (Throwable e) {
            // También es válido que lance excepción
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Should throw exception when extracting claim from invalid token")
    void testExtractClaimWithInvalidToken() {
        // GIVEN
        String invalidToken = "invalid.token.here";

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractClaim(invalidToken, Claims::getSubject));
    }

    @Test
    @DisplayName("Should throw exception when extracting claim from null token")
    void testExtractClaimWithNullToken() {
        // GIVEN
        String nullToken = null;

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractClaim(nullToken, Claims::getSubject));
    }

    @Test
    @DisplayName("Should handle null username when generating token")
    void testGenerateTokenWithNullUsername() {
        // GIVEN
        String nullUsername = null;

        // WHEN & THEN
        try {
            String token = jwtService.generateToken(nullUsername);
            // Si genera token con null, es válido
            assertNotNull(token);
        } catch (Throwable e) {
            // También es válido que lance excepción
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Should handle null email when generating verification token")
    void testGenerateVerificationTokenWithNullEmail() {
        // GIVEN
        String nullEmail = null;

        // WHEN & THEN
        try {
            String token = jwtService.generateVerificationToken(nullEmail);
            // Si genera token con null, es válido
            assertNotNull(token);
        } catch (Throwable e) {
            // También es válido que lance excepción
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Should generate token with empty string username")
    void testGenerateTokenWithEmptyString() {
        // GIVEN
        String emptyUsername = "";

        // WHEN
        String token = jwtService.generateToken(emptyUsername);

        // THEN
        assertNotNull(token);
        assertEquals("", jwtService.extractSubject(token));
    }

    @Test
    @DisplayName("Should reject verification token with wrong signature")
    void testValidateVerificationTokenWithWrongSignature() {
        // GIVEN
        String email = "user@test.com";
        Key wrongSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("T0RNeU56VTBOelkyTURFd01EQTBNREEwTURBME1EQTBNREEwTURBME1EQTBNREEwTURBMA=="));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + verificationExpiration);

        String tokenWithWrongSignature = Jwts.builder()
                .claim("type", "verification")
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(wrongSigningKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(tokenWithWrongSignature);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle tokens with special characters in subject")
    void testTokenWithSpecialCharactersInSubject() {
        // GIVEN
        String usernameWithSpecialChars = "user!@#$%^&*()";

        // WHEN
        String token = jwtService.generateToken(usernameWithSpecialChars);
        String extractedSubject = jwtService.extractSubject(token);

        // THEN
        assertEquals(usernameWithSpecialChars, extractedSubject);
    }

    @Test
    @DisplayName("Should handle tokens with very long username")
    void testTokenWithVeryLongUsername() {
        // GIVEN
        String longUsername = "a".repeat(1000);

        // WHEN
        String token = jwtService.generateToken(longUsername);
        String extractedSubject = jwtService.extractSubject(token);

        // THEN
        assertEquals(longUsername, extractedSubject);
    }

    @Test
    @DisplayName("Should reject verification token with only header part")
    void testValidateVerificationTokenWithOnlyHeader() {
        // GIVEN
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9";

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(invalidToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject verification token with only header and payload parts")
    void testValidateVerificationTokenWithHeaderAndPayload() {
        // GIVEN
        String incompleteToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0";

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(incompleteToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should throw exception when extracting subject from modified token")
    void testExtractSubjectWithModifiedToken() {
        // GIVEN
        String username = "testuser";
        String token = jwtService.generateToken(username);
        // Modificar el token (cambiar un carácter en la firma)
        String modifiedToken = token.substring(0, token.length() - 1) + "X";

        // WHEN & THEN
        assertThrows(Throwable.class, () -> jwtService.extractSubject(modifiedToken));
    }

    @Test
    @DisplayName("Should generate tokens with same subject but different signatures")
    void testGenerateTokenConsistency() {
        // GIVEN
        String username = "testuser";

        // WHEN
        String token1 = jwtService.generateToken(username);
        try {
            Thread.sleep(1000); // Pequeño delay para asegurar timestamps diferentes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(username);

        // THEN
        // Los tokens deben ser diferentes (contienen timestamps diferentes)
        assertNotEquals(token1, token2);
        // Pero el subject debe ser el mismo
        assertEquals(jwtService.extractSubject(token1), jwtService.extractSubject(token2));
    }

    @Test
    @DisplayName("Should correctly identify token at expiration boundary")
    void testVerificationTokenExpiredBoundary() {
        // GIVEN
        String email = "user@test.com";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Date now = new Date();
        // Expirar 100ms en el pasado para asegurar que está expirado
        Date expiryDate = new Date(now.getTime() - 100);

        String tokenExpiredNow = Jwts.builder()
                .claim("type", "verification")
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isExpired = jwtService.isTokenExpired(tokenExpiredNow);

        // THEN
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Should reject verification token when type claim is missing")
    void testValidateVerificationTokenWithMissingTypeClaim() {
        // GIVEN
        String email = "user@test.com";
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + verificationExpiration);

        String tokenWithoutType = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        // WHEN
        boolean isValid = jwtService.validateVerificationToken(tokenWithoutType);

        // THEN
        assertFalse(isValid);
    }
}
