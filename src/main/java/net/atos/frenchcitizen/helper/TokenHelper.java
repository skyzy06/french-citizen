package net.atos.frenchcitizen.helper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class TokenHelper {

    @Value("${token.validity.duration}")
    private Long validityDuration;

    @Value("${token.encryption.secret}")
    private String encryptionSecret;

    public String encode(Long id) {
        return JWT.create()
                .withSubject(id.toString())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(validityDuration)))
                .sign(Algorithm.HMAC256(encryptionSecret));
    }

    public DecodedJWT decode(String token) {
        try {
            return JWT.decode(token);
        } catch (JWTDecodeException ex) {
            return null;
        }
    }

    public boolean isValid(DecodedJWT token) {
        if (token == null) {
            return false;
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(encryptionSecret)).build();
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }
}
