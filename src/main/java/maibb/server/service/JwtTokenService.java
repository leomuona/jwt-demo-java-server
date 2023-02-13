package maibb.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import maibb.server.repository.RefreshTokenRepository;

@Service
public class JwtTokenService {

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.tokenValidity}")
    private long tokenValidity;

    @Value("${jwt.refreshTokenValidity}")
    private long refreshTokenValidity;

    @Value("${jwt.refreshCookieName}")
    private String refreshCookieName;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (this.secretKey == null) {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
        }
        return this.secretKey;
    }

    public String generateAccessToken(UserDetails userDetails) {
        final Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), this.tokenValidity);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, long validity) {
        final long nowMillis = System.currentTimeMillis();
        final long expMillis = nowMillis + (validity * 1000);

        final SecretKey key = getSecretKey();

        final JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuer(this.issuer)
                .setSubject(subject)
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .setId(UUID.randomUUID().toString());

        builder.signWith(key, SignatureAlgorithm.HS512);

        return builder.compact();
    }

    public String validateAccessToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        return getUsernameFromToken(token);
    }

    private String getUsernameFromToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        final Claims claims = parseClaimsFromToken(token);

        return claims.getSubject();
    }

    private Claims parseClaimsFromToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        final SecretKey key = getSecretKey();

        return Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(120) // 2 minutes
                .setSigningKey(key)
                .build().parseClaimsJws(token).getBody();
    }

    public String createRefreshToken(String username) {
        final Map<String, Object> claims = new HashMap<>();
        String token = doGenerateToken(claims, username, this.refreshTokenValidity);

        this.refreshTokenRepository.saveToken(token);

        return token;
    }

    public ResponseCookie generateRefreshCookie(String refreshToken, String path) {
        return ResponseCookie.from(this.refreshCookieName, refreshToken)
                .httpOnly(true) // hide from javascript
                .secure(true) // https only
                .path(path)
                .maxAge(this.refreshTokenValidity)
                .build();
    }

    public String validateRefreshToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {
        if (this.refreshTokenRepository.hasToken(token)) {
            try {
                return getUsernameFromToken(token);
            } catch (Exception ex) {
                this.refreshTokenRepository.deleteToken(token);

                throw ex;
            }
        }

        throw new IllegalArgumentException("Refresh token does not exist.");
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, this.refreshCookieName);
        if (cookie == null) {
            return null;
        }

        return cookie.getValue();
    }
}
