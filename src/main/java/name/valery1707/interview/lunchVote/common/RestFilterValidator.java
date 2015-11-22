package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.common.EntityUtilsBean.FILTER_OPERATION;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

public class RestFilterValidator implements Validator {
	@Override
	public boolean supports(Class<?> clazz) {
		return RestFilter.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		rejectIfAllNull(errors, "and", "or", "not", "field", "operation", "value");
		RestFilter filter = (RestFilter) target;
		otherMustBeNullIfThisNotNull(errors, "and", "or", "not", "field", "operation", "value");
		otherMustBeNullIfThisNotNull(errors, "or", "and", "not", "field", "operation", "value");
		otherMustBeNullIfThisNotNull(errors, "not", "and", "or", "field", "operation", "value");
		rejectIfEmptyNonNullCollection(errors, "and");
		rejectIfEmptyNonNullCollection(errors, "or");
		if (hasLength(filter.getField()) || hasLength(filter.getOperation()) || filter.getValue() != null) {
			fieldsMustBeNullIfTrue(errors, true, "and", "or", "not");
			rejectIfEmptyOrWhitespace(errors, "field", "must.have.value", new Object[0], "Field must have value");
			rejectIfEmptyOrWhitespace(errors, "operation", "must.have.value", new Object[0], "Field must have value");
			//Value must be exists for any operation exclude IS_NULL and NOT_NULL
			if (!trimToEmpty(filter.getOperation()).contains("_")) {
				rejectIfEmptyOrWhitespace(errors, "value", "must.have.value", new Object[0], "Field must have value");
			}
			//Operation must be known
			try {
				FILTER_OPERATION.byCode(filter.getOperation());
			} catch (IllegalArgumentException ex) {
				errors.rejectValue("operation", "unknown.filter.operation", new Object[]{filter.getOperation()}, "Unknown filter operation '{0}'");
			}
		}
	}

	private void rejectIfAllNull(Errors errors, String... fields) {
		for (String field : fields) {
			Object value = errors.getFieldValue(field);
			if (value != null) {
				return;
			}
		}
		errors.reject("at.least.one.field.must.have.value", new Object[0], "At least one field must have value");
	}

	private void rejectIfEmptyNonNullCollection(Errors errors, String field) {
		Object value = errors.getFieldValue(field);
		if (value instanceof Collection) {
			Collection collection = (Collection) value;
			if (collection.isEmpty()) {
				errors.rejectValue(field, "must.have.items", new Object[0], "Collection must have items");
			}
			for (Object filter : collection) {
				if (filter == null) {
					errors.rejectValue(field, "must.have.value", new Object[0], "Field must have value");
				}
				//todo Validate collection items
/*
				try {
					errors.pushNestedPath(field);
					ValidationUtils.invokeValidator(this, filter, errors);
				} finally {
					errors.popNestedPath();
				}
*/
			}
		}
	}

	private void otherMustBeNullIfThisNotNull(Errors errors, String thisField, String... otherFields) {
		Object thisValue = errors.getFieldValue(thisField);
		fieldsMustBeNullIfTrue(errors, thisValue != null, otherFields);
	}

	private void fieldsMustBeNullIfTrue(Errors errors, boolean state, String... otherFields) {
		if (state) {
			for (String field : otherFields) {
				rejectIfNotNull(errors, field);
			}
		}
	}

	private void rejectIfNotNull(Errors errors, String field) {
		Object value = errors.getFieldValue(field);
		if (value != null) {
			errors.rejectValue(field, "must.be.null", new Object[0], "Field must be null");
		}
	}
}
