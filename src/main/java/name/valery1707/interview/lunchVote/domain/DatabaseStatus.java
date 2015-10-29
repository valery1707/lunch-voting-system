package name.valery1707.interview.lunchVote.domain;

import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("unused")
public class DatabaseStatus {
	private String version;
	private String databaseName;
	private String username;
	private int sessionId;
	private int memoryUsed;
	private String lastAppliedMigration;

	public DatabaseStatus() {
	}

	public DatabaseStatus(String version, String databaseName, String username, int sessionId, int memoryUsed, String lastAppliedMigration) {
		this.version = version;
		this.databaseName = databaseName;
		this.username = username;
		this.sessionId = sessionId;
		this.memoryUsed = memoryUsed;
		this.lastAppliedMigration = lastAppliedMigration;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public int getMemoryUsed() {
		return memoryUsed;
	}

	public void setMemoryUsed(int memoryUsed) {
		this.memoryUsed = memoryUsed;
	}

	public String getLastAppliedMigration() {
		return lastAppliedMigration;
	}

	public void setLastAppliedMigration(String lastAppliedMigration) {
		this.lastAppliedMigration = lastAppliedMigration;
	}

	@Language("SQL")
	public static final String QUERY = "SELECT H2VERSION(), DATABASE(), USER(), SESSION_ID(), MEMORY_USED(), MAX(version) FROM schema_version";
	public static final RowMapper<DatabaseStatus> rowMapper = (rs, rowNum) ->
			new DatabaseStatus(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getString(6));
}
