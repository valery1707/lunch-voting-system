package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@RequestMapping("/account")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AccountController {

	@Inject
	private AccountRepo repo;

	@RequestMapping(method = RequestMethod.GET)
	public List<Account> list() {
		return repo.findAll();
	}
}
