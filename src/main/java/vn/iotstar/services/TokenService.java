package vn.iotstar.services;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Hop dong chung cho viec sinh/kiem tra JWT.
 * Co 2 cai dat:
 *  - JjwtTokenService   : dung thu vien io.jsonwebtoken (jjwt) - giong bai giang goc
 *  - NimbusTokenService : dung thu vien com.nimbusds:nimbus-jose-jwt (yeu cau 2)
 * Chon cai dat qua property: app.jwt.impl=jjwt | nimbus
 */
public interface TokenService {
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    long getExpirationTime();
}
