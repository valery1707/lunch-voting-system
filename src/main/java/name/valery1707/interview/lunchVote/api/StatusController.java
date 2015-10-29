package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.DatabaseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
public class StatusController {
	@Autowired
	private JdbcTemplate jt;

	@RequestMapping(value = "/database", method = RequestMethod.GET)
	public DatabaseStatus database() {
		return jt.queryForObject(DatabaseStatus.QUERY, DatabaseStatus.rowMapper);
	}
}
