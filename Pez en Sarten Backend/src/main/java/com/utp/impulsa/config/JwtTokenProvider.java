package com.utp.impulsa.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration}")
    private long jwtExpirationInMs;

    public String generateToken(UUID userId, String email, List<String> majors) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("email", email)
                .withClaim("majors", majors)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public UUID getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(token);
        return UUID.fromString(decodedJWT.getSubject());
    }

    public String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(token);
        return decodedJWT.getClaim("email").asString();
    }

    @SuppressWarnings("unchecked")
    public List<String> getMajorsFromToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build()
                .verify(token);
        return decodedJWT.getClaim("majors").asList(String.class);
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
