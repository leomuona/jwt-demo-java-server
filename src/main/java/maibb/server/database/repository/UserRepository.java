package maibb.server.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import maibb.server.database.mapper.UserRowMapper;
import maibb.server.database.model.DbUser;

@Repository
public class UserRepository {

	private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Optional<DbUser> getUser(String id) {
		final String query = "SELECT * FROM " + DbUser.TABLE + " WHERE " + //
				DbUser.ACTIVE + " IS TRUE AND " + DbUser.ID + " = ?";

		try {
			final DbUser user = jdbcTemplate.queryForObject(query, new UserRowMapper(), id);

			return Optional.of(user);
		} catch (IncorrectResultSizeDataAccessException e) {
			// user not found
		}

		return Optional.empty();
	}

	public Optional<DbUser> getUserByLogin(String login) {
		final String query = "SELECT * FROM " + DbUser.TABLE + " WHERE " + //
				DbUser.ACTIVE + " IS TRUE AND " + DbUser.LOGIN + " = ?";

		try {
			final DbUser user = jdbcTemplate.queryForObject(query, new UserRowMapper(), login);

			return Optional.of(user);
		} catch (IncorrectResultSizeDataAccessException e) {
			// user not found
		}

		return Optional.empty();
	}

	public DbUser saveUser(DbUser user) {
		if (user.getId() == null) {
			user.setId(UUID.randomUUID().toString());

			return insertUser(user);
		}

		return updateUser(user);
	}

	private DbUser insertUser(DbUser user) {
		final String query = "INSERT INTO " + DbUser.TABLE + " (" + //
				DbUser.ID + ", " + //
				DbUser.NAME + ", " + //
				DbUser.LOGIN + ", " + //
				DbUser.PASSWORD + ", " + //
				DbUser.ACTIVE + //
				") VALUES (?, ?, ?, ?, ?)";

		try {
			jdbcTemplate.update(query, //
					user.getId(), //
					user.getName(), //
					user.getLogin(), //
					user.getPassword(), //
					user.isActive());
		} catch (DataAccessException e) {
			log.error("Insert user failed", e);

			return null;
		}

		return user;
	}

	private DbUser updateUser(DbUser user) {
		// TODO implementation
		log.error("Update user: Not implemented yet");

		return null;
	}
}
