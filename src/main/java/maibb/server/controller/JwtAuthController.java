package maibb.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import maibb.server.record.LoginRequest;
import maibb.server.record.AuthenticatedUser;
import maibb.server.record.JwtResponse;
import maibb.server.record.MessageResponse;
import maibb.server.service.JwtTokenService;

@RestController
public class JwtAuthController {

    public static final String PATH_AUTH_PREFIX = "/auth";
    public static final String PATH_LOGIN = PATH_AUTH_PREFIX + "/login";
    public static final String PATH_REFRESH_TOKEN = PATH_AUTH_PREFIX + "/refresh";
    public static final String PATH_AUTHENTICATED_USER = PATH_AUTH_PREFIX + "/authenticateduser";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = PATH_LOGIN, method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {

        authenticate(loginRequest.username(), loginRequest.password());

        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(loginRequest.username());

        final String accessToken = this.jwtTokenService.generateAccessToken(userDetails);

        final String refreshToken = this.jwtTokenService.createRefreshToken(userDetails.getUsername());
        final ResponseCookie refreshCookie = this.jwtTokenService.generateRefreshCookie(refreshToken, "/");

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new JwtResponse(accessToken));
    }

    private void authenticate(String username, String password) throws AuthenticationException {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @RequestMapping(value = PATH_REFRESH_TOKEN, method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthToken(HttpServletRequest request) throws Exception {
        String refreshToken = this.jwtTokenService.getRefreshTokenFromCookie(request);
        if (refreshToken != null && refreshToken.length() > 0) {
            String username = this.jwtTokenService.validateRefreshToken(refreshToken);

            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            final String accessToken = this.jwtTokenService.generateAccessToken(userDetails);

            return ResponseEntity.ok(new JwtResponse(accessToken));
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is empty."));
    }

    @RequestMapping(value = PATH_AUTHENTICATED_USER, method = RequestMethod.GET)
    public ResponseEntity<?> authenticatedUser() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String name = authentication.getName();

        return ResponseEntity.ok(new AuthenticatedUser(name));
    }
}
