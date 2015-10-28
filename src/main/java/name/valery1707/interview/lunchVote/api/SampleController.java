package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.HelloEntity;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Description("A controller for handling requests for hello messages")
@RequestMapping("/sample")
public class SampleController {

	@RequestMapping(method = RequestMethod.GET)
	public HelloEntity hello() {
		return new HelloEntity("Hello");
	}
}
