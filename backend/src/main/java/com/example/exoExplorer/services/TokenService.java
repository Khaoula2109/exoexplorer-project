package com.example.exoExplorer.services;

import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * Service responsible for JWT token operations.
 * Implements the Token Provider pattern.
 */
@Service
public class TokenService {

    @Value("${jwt.secret:MySuperSecretKeyForJWTMySuperSecretKeyForJWT}")
    private String secretKey;

    @Value("${jwt.expiration:3600000}") // 1 hour by default
    private long jwtExpiration;

    private SecretKey key;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lazily initializes the secret key.
     * Using lazy initialization to ensure value is properly injected.
     */
    public SecretKey getKey() {
        if (key == null) {
            key = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
        return key;
    }

    /**
     * Generates a JWT token for a user with their roles.
     * @param email The user's email
     * @return The generated JWT token
     */
    public String generateToken(String email) {
        // Recherchez l'utilisateur pour obtenir ses rôles
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Créez une map pour les claims supplémentaires
        Map<String, Object> claims = new HashMap<>();

        // Ajoutez les rôles comme claim
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");  // Tous les utilisateurs ont ce rôle

        if (user.isAdmin()) {
            roles.add("ROLE_ADMIN");  // Ajoutez le rôle admin si applicable
        }

        // Ajoutez les rôles aux claims
        claims.put("roles", roles);

        // Générez le token avec les claims supplémentaires
        return generateToken(claims, email);
    }

    /**
     * Generates a JWT token with additional claims.
     * @param extraClaims Additional claims to include
     * @param email The user's email
     * @return The generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (email) from a token.
     * @param token The JWT token
     * @return The extracted email or null if token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates a token against user details.
     * @param token The JWT token to validate
     * @param userDetails The user details to validate against
     * @return True if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            // Token is expired, so it's not valid
            return false;
        } catch (Exception e) {
            // Any other exception means token is not valid
            return false;
        }
    }

    /**
     * Checks if a token is expired.
     * @param token The JWT token
     * @return True if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extracts the expiration date from a token.
     * @param token The JWT token
     * @return The expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a token.
     * @param token The JWT token
     * @param claimsResolver Function to extract a specific claim
     * @param <T> The type of the claim
     * @return The extracted claim
     * @throws ExpiredJwtException if the token is expired
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a token.
     * @param token The JWT token
     * @return All claims from the token
     * @throws ExpiredJwtException if the token is expired
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}