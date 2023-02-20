package maibb.server.database.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import maibb.server.database.model.DbRefreshToken;

public class RefreshTokenRowMapper implements RowMapper<DbRefreshToken> {

	@Override
	public DbRefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
		final DbRefreshToken refreshToken = new DbRefreshToken();

		refreshToken.setJti(rs.getString(DbRefreshToken.JTI));
		refreshToken.setToken(rs.getString(DbRefreshToken.TOKEN));
		refreshToken.setExpiry(rs.getDate(DbRefreshToken.EXPIRY));

		return refreshToken;
	}

}
