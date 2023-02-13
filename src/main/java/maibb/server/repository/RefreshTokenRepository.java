package maibb.server.repository;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    // TODO database, this is just in memory
    private Set<String> tokens;

    public RefreshTokenRepository() {
        this.tokens = new HashSet<>();
    }

    public void saveToken(String token) {
        if (!this.tokens.contains(token)) {
            this.tokens.add(token);
        }
    }

    public boolean hasToken(String token) {
        return this.tokens.contains(token);
    }

    public void deleteToken(String token) {
        this.tokens.remove(token);
        
    }
}
