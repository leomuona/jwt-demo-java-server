package maibb.server.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import maibb.server.database.model.DbUser;
import maibb.server.database.repository.UserRepository;
import maibb.server.record.UserData;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public UserData getUser(String userId) {
		Optional<DbUser> optionalUser = userRepository.getUser(userId);
		if (optionalUser.isEmpty()) {
			throw new RuntimeException("Could not find user with identifier <" + userId + ">");
		}

		DbUser user = optionalUser.get();

		return new UserData(user.getId(), user.getName(), user.getLogin());
	}

	public UserData getUserByLogin(String login) {
		Optional<DbUser> optionalUser = userRepository.getUserByLogin(login);
		if (optionalUser.isEmpty()) {
			throw new RuntimeException("Could not find user with login <" + login + ">");
		}

		DbUser user = optionalUser.get();

		return new UserData(user.getId(), user.getName(), user.getLogin());
	}

	public UserData registerUser(String name, String login, String rawPassword) {
		notNullOrEmpty(name, "name");
		notNullOrEmpty(login, "login");
		notNullOrEmpty(rawPassword, "rawPassword");

		if (userRepository.getUserByLogin(login).isPresent()) {
			throw new IllegalArgumentException("Login <" + login + "> already exists.");
		}

		DbUser newUser = new DbUser();
		newUser.setName(name);
		newUser.setLogin(login);
		newUser.setActive(true);
		newUser.setPassword(passwordEncoder.encode(rawPassword));

		DbUser user = userRepository.saveUser(newUser);

		return new UserData(user.getId(), user.getName(), user.getLogin());
	}

	private static void notNullOrEmpty(String value, String param) throws IllegalArgumentException {
		if (value == null) {
			throw new IllegalArgumentException("Given " + param + " value is null!");
		}
		if (value.length() < 1) {
			throw new IllegalArgumentException("Given " + param + " value is empty!");
		}
	}
}
