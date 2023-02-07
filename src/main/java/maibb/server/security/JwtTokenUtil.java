package maibb.server.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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

@Component
public class JwtTokenUtil {

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.tokenValidity}")
    private long tokenValidity;

    private SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (this.secretKey == null) {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
        }
        return this.secretKey;
    }

    public String generateToken(UserDetails userDetails) {
        final Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final long nowMillis = System.currentTimeMillis();
        final long expMillis = nowMillis + ((this.tokenValidity > 0 ? this.tokenValidity : 3600) * 1000);

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

    public String getUsernameFromToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
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

}
