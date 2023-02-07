package maibb.server.controller;

import java.util.concurrent.atomic.AtomicLong;

import maibb.server.record.Message;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/hello")
	public Message hello() {
		final String name = getName();
		return new Message(counter.incrementAndGet(), String.format(template, name));
	}

	private String getName() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getName();
	}
}
