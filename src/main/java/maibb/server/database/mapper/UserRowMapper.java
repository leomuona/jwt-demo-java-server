package maibb.server.database.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import maibb.server.database.model.DbUser;

public class UserRowMapper implements RowMapper<DbUser> {

	@Override
	public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException {
		final DbUser user = new DbUser();

		user.setId(rs.getString(DbUser.ID));
		user.setName(rs.getString(DbUser.NAME));
		user.setLogin(rs.getString(DbUser.LOGIN));
		user.setPassword(rs.getString(DbUser.PASSWORD));
		user.setActive(rs.getBoolean(DbUser.ACTIVE));

		return user;
	}

}
