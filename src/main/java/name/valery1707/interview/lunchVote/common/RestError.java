package name.valery1707.interview.lunchVote.common;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class RestError {
	private final ZonedDateTime timestamp;
	private final int status;
	private final String error;
	private final String exception;
	private final String message;
	private final String path;

	public RestError(ZonedDateTime timestamp, int status, String error, String exception, String message, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.exception = exception;
		this.message = message;
		this.path = path;
	}

	public RestError(int status, String error, String exception, String message, String path) {
		this(ZonedDateTime.now(), status, error, exception, message, path);
	}

	public RestError(HttpStatus status, String exception, String message, String path) {
		this(status.value(), status.getReasonPhrase(), exception, message, path);
	}

	public static RestError fromException(RuntimeException ex, String path) {
		return new RestError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getCanonicalName(), ex.getLocalizedMessage(), path);
	}

	@SuppressWarnings("unused")
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	@SuppressWarnings("unused")
	public int getStatus() {
		return status;
	}

	@SuppressWarnings("unused")
	public String getError() {
		return error;
	}

	@SuppressWarnings("unused")
	public String getException() {
		return exception;
	}

	@SuppressWarnings("unused")
	public String getMessage() {
		return message;
	}

	@SuppressWarnings("unused")
	public String getPath() {
		return path;
	}
}
