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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import maibb.server.record.LoginRequest;
import maibb.server.record.AuthenticatedUserResponse;
import maibb.server.record.JwtResponse;
import maibb.server.record.MessageResponse;
import maibb.server.record.RegisterUserRequest;
import maibb.server.record.UserData;
import maibb.server.service.JwtTokenService;
import maibb.server.service.UserService;

@RestController
public class JwtAuthController {

	public static final String PATH_AUTH_PREFIX = "/auth";
	public static final String PATH_LOGIN = PATH_AUTH_PREFIX + "/login";
	public static final String PATH_REFRESH_TOKEN = PATH_AUTH_PREFIX + "/refresh";
	public static final String PATH_AUTHENTICATED_USER = PATH_AUTH_PREFIX + "/authenticateduser";
	public static final String PATH_REGISTER = PATH_AUTH_PREFIX + "/register";

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Autowired
	private UserService userService;

	@RequestMapping(value = PATH_LOGIN, method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {

		this.authenticate(loginRequest.login(), loginRequest.password());

		final UserData user = this.userService.getUserByLogin(loginRequest.login());

		final String accessToken = this.jwtTokenService.generateAccessToken(user.id());

		final String refreshToken = this.jwtTokenService.createRefreshToken(user.id());
		final ResponseCookie refreshCookie = this.jwtTokenService.generateRefreshCookie(refreshToken, "/");

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(new JwtResponse(accessToken));
	}

	private void authenticate(String login, String password) throws AuthenticationException {
		this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
	}

	@RequestMapping(value = PATH_REFRESH_TOKEN, method = RequestMethod.GET)
	public ResponseEntity<?> refreshAuthToken(HttpServletRequest request) throws Exception {
		final String refreshToken = this.jwtTokenService.getRefreshTokenFromCookie(request);
		if (refreshToken != null && refreshToken.length() > 0) {
			String userId = this.jwtTokenService.validateRefreshToken(refreshToken);

			final String accessToken = this.jwtTokenService.generateAccessToken(userId);

			return ResponseEntity.ok(new JwtResponse(accessToken));
		}

		return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is empty."));
	}

	@RequestMapping(value = PATH_AUTHENTICATED_USER, method = RequestMethod.GET)
	public ResponseEntity<?> authenticatedUser() throws Exception {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		final String userId = authentication.getName();
		final UserData user = userService.getUser(userId);

		return ResponseEntity.ok(new AuthenticatedUserResponse(user.id(), user.name()));
	}

	@RequestMapping(value = PATH_REGISTER, method = RequestMethod.POST)
	public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest registerUserRequest) throws Exception {
		try {
			userService.registerUser(registerUserRequest.name(), registerUserRequest.login(),
					registerUserRequest.password());

			return ResponseEntity.ok().body(new MessageResponse("Registeration complete. You can now login."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new MessageResponse("Request invalid or username already exists."));
		}
	}
}
