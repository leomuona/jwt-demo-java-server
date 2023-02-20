package maibb.server.security;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import maibb.server.record.UserData;
import maibb.server.service.JwtTokenService;
import maibb.server.service.UserService;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

	@Autowired
	private UserService userService;

	@Autowired
	private JwtTokenService jwtTokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader("Authorization");

		String userId = null;
		String jwtToken = null;

		// header is in the form of "Bearer token", prefix is 7 chars long
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				userId = this.jwtTokenService.validateAccessToken(jwtToken);
			} catch (Exception ex) {
				log.warn("Could not authorize user: " + ex.getLocalizedMessage());
			}
		}

		// get user and set authentication
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserData user = this.userService.getUser(userId);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					user.id(), null, new ArrayList<>());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

}
