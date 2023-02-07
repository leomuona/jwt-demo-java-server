package maibb.server.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class NotSafePasswordEncoder implements PasswordEncoder {

    // TODO: delete this and use actual password encoder

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String password = rawPassword.toString();
        return password.equals(encodedPassword);
    }

}
