package maibb.server.controller;

import java.util.concurrent.atomic.AtomicLong;

import maibb.server.record.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/hello")
	public Message hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Message(counter.incrementAndGet(), String.format(template, name));
	}

}
