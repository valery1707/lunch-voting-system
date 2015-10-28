package name.valery1707.interview.lunchVote.api;

import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Description("A controller for handling requests for hello messages")
@RequestMapping("/sample")
public class SampleController {

	@RequestMapping(method = RequestMethod.GET)
	public String hello() {
		return "Hello";
	}
}
