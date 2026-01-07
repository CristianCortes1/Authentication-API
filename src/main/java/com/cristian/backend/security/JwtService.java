package com.cristian.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.verification.expiration}")
    private long verificationExpiration;

    /**
     * Genera un token JWT para autenticación de usuario
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, jwtExpiration);
    }

    /**
     * Genera un token JWT para verificación de email
     */
    public String generateVerificationToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "verification");
        return createToken(claims, email, verificationExpiration);
    }

    /**
     * Crea el token JWT
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el username/email del token
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error al extraer claims del token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Valida el token JWT
     */
    public boolean validateToken(String token, String subject) {
        try {
            final String tokenSubject = extractSubject(token);
            return (tokenSubject.equals(subject) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida el token de verificación
     */
    public boolean validateVerificationToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return "verification".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error al validar token de verificación: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la clave de firma
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

