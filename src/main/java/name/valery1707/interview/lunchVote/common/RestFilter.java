package name.valery1707.interview.lunchVote.common;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

import static java.util.Arrays.asList;

public class RestFilter {
	public enum Type {
		AND, OR, NOT, FILTER
	}

	@Size(min = 1)
	@Valid
	private List<RestFilter> and;
	@Size(min = 1)
	@Valid
	private List<RestFilter> or;
	@Valid
	private RestFilter not;

	private String field;
	private String operation;
	private Object value;

	//region Constructor

	public RestFilter() {
	}

	RestFilter(List<RestFilter> and, List<RestFilter> or, RestFilter not, String field, String operation, Object value) {
		this();
		this.and = and;
		this.or = or;
		this.not = not;
		this.field = field;
		this.operation = operation;
		this.value = value;
	}

	//endregion

	//region Factory methods

	public static RestFilter and(RestFilter... filters) {
		return and(asList(filters));
	}

	public static RestFilter and(List<RestFilter> filters) {
		return new RestFilter(filters, null, null, null, null, null);
	}

	public static RestFilter or(RestFilter... filters) {
		return or(asList(filters));
	}

	public static RestFilter or(List<RestFilter> filters) {
		return new RestFilter(null, filters, null, null, null, null);
	}

	public static RestFilter not(RestFilter filter) {
		return new RestFilter(null, null, filter, null, null, null);
	}

	public static RestFilter filter(String field, String operation, @Nullable Object value) {
		return new RestFilter(null, null, null, field, operation, value);
	}

	//endregion

	//region Getters and Setters

	public List<RestFilter> getAnd() {
		return and;
	}

	public void setAnd(List<RestFilter> and) {
		this.and = and;
	}

	public List<RestFilter> getOr() {
		return or;
	}

	public void setOr(List<RestFilter> or) {
		this.or = or;
	}

	public RestFilter getNot() {
		return not;
	}

	public void setNot(RestFilter not) {
		this.not = not;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	//endregion

	public Type getType() {
		if (getAnd() != null) {
			return Type.AND;
		}
		if (getOr() != null) {
			return Type.OR;
		}
		if (getNot() != null) {
			return Type.NOT;
		}
		return Type.FILTER;
	}
}
