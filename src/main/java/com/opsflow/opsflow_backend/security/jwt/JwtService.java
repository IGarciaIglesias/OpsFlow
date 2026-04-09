package com.opsflow.opsflow_backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET =
            "opsflow-secret-key-very-long-and-secure-256-bit";

    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    // Generar token CON ROLE
    public String generateToken(UserDetails userDetails) {

        String role = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)
                ))
                .compact();
    }

    // Extraer username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extraer role (LO USARÁ FRONT + SECURITY)
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Validar token
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)
                ))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
