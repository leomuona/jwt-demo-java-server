package maibb.server.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import maibb.server.database.model.DbUser;
import maibb.server.database.repository.UserRepository;

// This is just for Spring Security things.
// Do not add additional stuff here.

@Service("userDetailsService")
public class SecurityUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<DbUser> optionalUser = userRepository.getUserByLogin(username);
		if (optionalUser.isPresent()) {
			DbUser user = optionalUser.get();
			return new User(user.getLogin(), user.getPassword(), new ArrayList<>());
		}

		throw new UsernameNotFoundException("User not found with username: " + username);
	}

}
