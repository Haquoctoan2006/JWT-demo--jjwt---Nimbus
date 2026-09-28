package vn.iotstar.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.iotstar.exception.InvalidTokenException;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

/**
 * Bai tap 2: thay the thu vien jjwt bang Nimbus JOSE + JWT
 * (com.nimbusds:nimbus-jose-jwt), giu nguyen hanh vi/API (TokenService)
 * de co the hoan doi cho JjwtTokenService ma khong doi Filter/Controller.
 *
 * Kich hoat bang: app.jwt.impl=nimbus  (trong application.properties)
 *
 * Secret key duoc giai ma Base64 giong JjwtTokenService, nen token sinh ra o
 * 2 che do co cung chu ky (co the doi qua lai ma khong lam hong token cu).
 */
@Service
@ConditionalOnProperty(name = "app.jwt.impl", havingValue = "nimbus")
public class NimbusTokenService implements TokenService {

    @Value("${security.jwt.secret-key}")
    private String secretKey; // Base64 (toi thieu 32 byte sau khi giai ma cho HS256)

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private byte[] getKeyBytes() {
        return Base64.getDecoder().decode(secretKey);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        try {
            long now = System.currentTimeMillis();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(new Date(now))
                    .expirationTime(new Date(now + jwtExpiration))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(new MACSigner(getKeyBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Khong the tao JWT (Nimbus): " + e.getMessage(), e);
        }
    }

    /** Parse + kiem tra chu ky + kiem tra het han (giong jjwt: sai/het han thi nem loi). */
    private JWTClaimsSet parseAndVerify(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(new MACVerifier(getKeyBytes()))) {
                throw new InvalidTokenException("JWT signature does not match locally computed signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || !expiration.after(new Date())) {
                throw new InvalidTokenException("JWT expired at " + expiration);
            }
            return claims;
        } catch (ParseException e) {
            throw new InvalidTokenException("Invalid JWT string: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new InvalidTokenException("Cannot verify JWT: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractUsername(String token) {
        return parseAndVerify(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = parseAndVerify(token).getSubject();
            return userDetails.getUsername().equals(username);
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
