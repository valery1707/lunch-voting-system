package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private AccountRepo repo;

	@RequestMapping(method = RequestMethod.GET)
	public List<Account> list() {
		return repo.findAll();
	}
}
