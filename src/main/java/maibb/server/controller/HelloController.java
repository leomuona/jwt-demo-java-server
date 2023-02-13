package maibb.server.controller;

import maibb.server.record.MessageResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	private static final String template = "Hello, %s!";

	@GetMapping("/hello")
	public MessageResponse hello() {
		final String name = getName();
		return new MessageResponse(String.format(template, name));
	}

	private String getName() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getName();
	}
}
