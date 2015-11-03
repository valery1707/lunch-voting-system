package name.valery1707.interview.lunchVote.common;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ErrorMessage {
	private final String fieldName;
	private final String messageCode;
	private final List<Object> params;

	public ErrorMessage(String fieldName, String messageCode, List<Object> params) {
		this.fieldName = fieldName;
		this.messageCode = messageCode;
		this.params = params;
	}

	public ErrorMessage(String fieldName, String messageCode, Object... params) {
		this(fieldName, messageCode, Arrays.asList(params));
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getMessageCode() {
		return messageCode;
	}

	public List<Object> getParams() {
		return unmodifiableList(params);
	}
}
