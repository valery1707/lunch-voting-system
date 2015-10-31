package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.DatabaseStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping("/status")
@PreAuthorize("hasRole('ROLE_GUEST')")
public class StatusController {
	@Inject
	private JdbcTemplate jt;

	@RequestMapping(value = "/database", method = RequestMethod.GET)
	public DatabaseStatus database() {
		return jt.queryForObject(DatabaseStatus.QUERY, DatabaseStatus.rowMapper);
	}
}
