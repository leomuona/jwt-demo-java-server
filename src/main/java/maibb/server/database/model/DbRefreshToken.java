package maibb.server.database.model;

import java.util.Date;

public class DbRefreshToken {

	public static final String TABLE = "refresh_tokens";

	public static final String JTI = "jti";
	public static final String TOKEN = "token";
	public static final String EXPIRY = "expiry";

	private String jti;
	private String token;
	private Date expiry;

	public DbRefreshToken() {
	}

	public DbRefreshToken(String jti, String token, Date expiry) {
		this.jti = jti;
		this.token = token;
		this.expiry = expiry;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

}
