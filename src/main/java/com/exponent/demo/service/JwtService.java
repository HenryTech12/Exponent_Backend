package com.exponent.demo.service;

import com.exponent.demo.dto.UserData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private String key = "";
    public JwtService() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        SecretKey secretKey = keyGenerator.generateKey();
        key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String generateToken(UserData userData) {
        Map<String,String> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(userData.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() * 1000 + 30))
                .signWith(getKey())
                .compact();

    }

    public SecretKey getKey() {
        byte[] secretBytes = Base64.getDecoder().decode(key);
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public <T>T extractClaims(String token, Function<Claims,T> claimsResolver) {
        Claims claims = extractClaim(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractClaim(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        Date currentDate = new Date(System.currentTimeMillis());
        return (username.equals(userDetails.getUsername()) && currentDate.before(extractClaims(token,Claims::getExpiration)));
    }

    public String extractUsername(String token) {
        return extractClaims(token,Claims::getSubject);
    }
}
