package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.DatabaseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController {
	@Autowired
	private JdbcTemplate jt;

	@RequestMapping(value = "/database", method = RequestMethod.GET)
	public DatabaseStatus database() {
		return new DatabaseStatus(callDatabaseFunction("H2VERSION"), callDatabaseFunction("DATABASE"), callDatabaseFunction("USER"), callDatabaseFunction("SESSION_ID"), callDatabaseFunction("MEMORY_USED"));
	}

	protected String callDatabaseFunction(String functionName) {
		String sql = "CALL " + functionName + "();";
		if (jt == null) {
			return sql;
		}
		List<String> result = jt.query(sql, (rs, rowNum) -> {
			return rs.getString(1);
		});
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}
}
