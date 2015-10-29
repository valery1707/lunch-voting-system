package name.valery1707.interview.lunchVote.domain;

public class DatabaseStatus {
	private String version;
	private String databaseName;
	private String username;
	private String sessionId;
	private String memoryUsed;

	public DatabaseStatus() {
	}

	public DatabaseStatus(String version, String databaseName, String username, String sessionId, String memoryUsed) {
		this.version = version;
		this.databaseName = databaseName;
		this.username = username;
		this.sessionId = sessionId;
		this.memoryUsed = memoryUsed;
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getMemoryUsed() {
		return memoryUsed;
	}

	public void setMemoryUsed(String memoryUsed) {
		this.memoryUsed = memoryUsed;
	}
}
