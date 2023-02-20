package maibb.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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
import maibb.server.database.model.DbRefreshToken;
import maibb.server.database.repository.RefreshTokenRepository;

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

	public String generateAccessToken(String userId) {
		final Map<String, Object> claims = new HashMap<>();
		JwtToken jwtToken = doGenerateToken(claims, userId, this.tokenValidity);

		return jwtToken.token();
	}

	private JwtToken doGenerateToken(Map<String, Object> claims, String subject, long validity) {
		final long nowMillis = System.currentTimeMillis();
		final long expMillis = nowMillis + (validity * 1000);

		final Date expiryDate = new Date(expMillis);
		final String jti = UUID.randomUUID().toString();

		final SecretKey key = getSecretKey();

		final JwtBuilder builder = Jwts.builder()
				.setClaims(claims)
				.setIssuer(this.issuer)
				.setSubject(subject)
				.setIssuedAt(new Date(nowMillis))
				.setExpiration(expiryDate)
				.setId(jti);

		builder.signWith(key, SignatureAlgorithm.HS512);

		final String token = builder.compact();

		return new JwtToken(jti, token, expiryDate);
	}

	public String validateAccessToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, SignatureException, IllegalArgumentException {
		final Claims claims = parseClaimsFromToken(token);

		return claims.getSubject();
	}

	// throws exception token expired
	private Claims parseClaimsFromToken(String token) throws ExpiredJwtException, UnsupportedJwtException,
			MalformedJwtException, SignatureException, IllegalArgumentException {
		final SecretKey key = getSecretKey();

		return Jwts.parserBuilder()
				.setAllowedClockSkewSeconds(120) // 2 minutes
				.setSigningKey(key)
				.build().parseClaimsJws(token).getBody();
	}

	public String createRefreshToken(String userId) {
		final Map<String, Object> claims = new HashMap<>();
		JwtToken jwtToken = doGenerateToken(claims, userId, this.refreshTokenValidity);

		this.refreshTokenRepository.saveToken(new DbRefreshToken(jwtToken.jti(), jwtToken.token(), jwtToken.expiry()));

		return jwtToken.token();
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
		Claims claims;
		try {
			claims = parseClaimsFromToken(token);
		} catch (ExpiredJwtException e) {
			this.refreshTokenRepository.deleteExpiredTokens();

			throw e;
		}

		Optional<DbRefreshToken> existingToken = this.refreshTokenRepository.getToken(claims.getId());

		if (existingToken.isEmpty()) {
			throw new IllegalArgumentException("Token does not exist");
		}

		return claims.getSubject();
	}

	public String getRefreshTokenFromCookie(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, this.refreshCookieName);
		if (cookie == null) {
			return null;
		}

		return cookie.getValue();
	}

	// just for internal wrapping
	private record JwtToken(String jti, String token, Date expiry) {
	}
}
