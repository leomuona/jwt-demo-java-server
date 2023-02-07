package maibb.server.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: something else than this :D

        if (username.equals("mikki")) {
            return new User("mikki", "TopSecret!", new ArrayList<>());
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

}
