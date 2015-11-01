package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.dto.Hello;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Description("A controller for handling requests for hello messages")
@RequestMapping("/hello")
public class HelloController {

	@RequestMapping(method = RequestMethod.GET)
	public Hello hello() {
		return new Hello("Hello");
	}
}
