package name.valery1707.interview.lunchVote.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestResult<T> {
	private boolean valid;
	private List<ErrorMessage> errors = new ArrayList<>();
	private T result;

	public RestResult() {
	}

	public RestResult(boolean valid, T result) {
		this();
		this.valid = valid;
		this.result = result;
	}

	public RestResult(T result) {
		this(result != null, result);
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public List<ErrorMessage> getErrors() {
		return Collections.unmodifiableList(errors);
	}

	public RestResult<T> addError(ErrorMessage message) {
		errors.add(message);
		return this;
	}

	public RestResult<T> addError(String field, String messageCode, Object... params) {
		return addError(new ErrorMessage(field, messageCode, params));
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
}
