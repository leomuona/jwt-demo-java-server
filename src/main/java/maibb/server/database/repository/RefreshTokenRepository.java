package maibb.server.database.repository;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import maibb.server.database.mapper.RefreshTokenRowMapper;
import maibb.server.database.model.DbRefreshToken;

@Repository
public class RefreshTokenRepository {

	private static final Logger log = LoggerFactory.getLogger(RefreshTokenRepository.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void saveToken(DbRefreshToken token) {
		final Optional<DbRefreshToken> existing = getToken(token.getJti());
		if (existing.isPresent()) {
			log.info("Save called on already existing token -- skipping");
			return;
		}

		final String query = "INSERT INTO " + DbRefreshToken.TABLE + " (" + //
				DbRefreshToken.JTI + ", " + //
				DbRefreshToken.TOKEN + ", " + //
				DbRefreshToken.EXPIRY + //
				") VALUES (?, ?, ?)";

		try {
			jdbcTemplate.update(query, //
					token.getJti(), //
					token.getToken(), //
					token.getExpiry());
		} catch (DataAccessException e) {
			log.error("Insert refresh token failed", e);
		}
	}

	public Optional<DbRefreshToken> getToken(String jti) {
		final String query = "SELECT * FROM " + DbRefreshToken.TABLE + " WHERE " + DbRefreshToken.JTI + " = ?";

		try {
			final DbRefreshToken token = jdbcTemplate.queryForObject(query, new RefreshTokenRowMapper(), jti);

			if (token.getExpiry().before(new Date())) {
				this.deleteToken(token.getJti());

				return Optional.empty();
			}

			return Optional.of(token);
		} catch (IncorrectResultSizeDataAccessException e) {
			// token not found
		}

		return Optional.empty();
	}

	public void deleteToken(String jti) {
		final String query = "DELETE FROM " + DbRefreshToken.TABLE + " WHERE " + DbRefreshToken.JTI + " = ?";

		try {
			jdbcTemplate.update(query, jti);
		} catch (DataAccessException e) {
			log.error("Delete refresh token failed", e);
		}
	}

	public int deleteExpiredTokens() {
		final String query = "DELETE FROM " + DbRefreshToken.TABLE + " WHERE " + DbRefreshToken.EXPIRY
				+ " < NOW()";

		try {
			return jdbcTemplate.update(query);
		} catch (DataAccessException e) {
			log.error("Failed to delete expired refresh tokens", e);

			return 0;
		}
	}
}
