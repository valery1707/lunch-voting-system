package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.common.EntityUtilsBean.FilterOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static name.valery1707.interview.lunchVote.common.RestFilter.filter;
import static name.valery1707.interview.lunchVote.common.RestFilter.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestFilterValidatorTest {

	private RestFilterValidator validator;

	@Before
	public void setUp() throws Exception {
		validator = new RestFilterValidator();
	}

	private Errors validate(RestFilter value) {
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(value, "");
		validator.validate(value, errors);
		return errors;
	}

	@Test
	public void testSupports() throws Exception {
		assertTrue(validator.supports(RestFilter.class));
		assertFalse(validator.supports(Object.class));
	}

	@Test
	public void testGlobal() throws Exception {
		Errors errors = validate(filter(null, null, null));
		assertThat(errors.getGlobalErrors()).as("global")
				.hasSize(1)
				.extracting(DefaultMessageSourceResolvable::getCode)
				.containsOnly("at.least.one.field.must.have.value");
		assertThat(errors.getFieldErrors()).as("field")
				.isEmpty();
	}

	@Test
	public void testFilter() throws Exception {
		Errors errors;

		errors = validate(filter("field", null, null));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(3)
				.extracting(FieldError::getField)
				.containsOnly("operation", "value");

		errors = validate(filter("field", FilterOperation.GREATER.getCode(), null));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly("value");

		errors = validate(filter("field", FilterOperation.GREATER.getCode(), 10));
		assertThat(errors.getFieldErrors()).as("field")
				.isEmpty();

		errors = validate(filter(null, FilterOperation.GREATER.getCode(), null));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(2)
				.extracting(FieldError::getField)
				.containsOnly("field", "value");

		errors = validate(filter(null, FilterOperation.IS_NULL.getCode(), null));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly("field");

		errors = validate(filter(null, FilterOperation.GREATER.getCode(), 10));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly("field");

		errors = validate(filter(null, null, 42));
		assertThat(errors.getFieldErrors()).as("field")
				.hasSize(3)
				.extracting(FieldError::getField)
				.containsOnly("field", "operation");
	}

	@Test
	public void testAnd() throws Exception {
		testCollectionField("and", RestFilter::and);
	}

	@Test
	public void testOr() throws Exception {
		testCollectionField("or", RestFilter::or);
	}

	private void testCollectionField(String field, Function<RestFilter[], RestFilter> constructor) {
		Errors errors;

		errors = validate(constructor.apply(new RestFilter[0]));
		assertThat(errors.getFieldErrors()).as("field[%s]", field)
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly(field);

		errors = validate(constructor.apply(new RestFilter[]{null}));
		assertThat(errors.getFieldErrors()).as("field[%s]", field)
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly(field);

		//todo Validate collection items
/*
		errors = validate(constructor.apply(new RestFilter[]{filter(null, null, null)}));
		assertThat(errors.getFieldErrors()).as("field[%s]", field)
				.hasSize(1)
				.extracting(FieldError::getField)
				.containsOnly(field);
*/

		RestFilter filter = constructor.apply(new RestFilter[]{filterEqual42()});
		errors = validate(filter);
		assertThat(errors.getFieldErrors()).as("field[%s]", field)
				.isEmpty();
		testCollectionWithOther(filter, field, "not", RestFilter::setNot, filterEqual42());
		testCollectionWithOther(filter, field, "field", RestFilter::setField, "someField");
		testCollectionWithOther(filter, field, "operation", RestFilter::setOperation, FilterOperation.EQUAL.getCode());
		//noinspection UnnecessaryBoxing
		testCollectionWithOther(filter, field, "value", RestFilter::setValue, Integer.valueOf(42));
		if (field.equals("and")) {
			testCollectionWithOther(filter, field, "or", RestFilter::setOr, singletonList(filterEqual42()));
		} else {
			testCollectionWithOther(filter, field, "and", RestFilter::setAnd, singletonList(filterEqual42()));
		}
	}

	private <T> void testCollectionWithOther(RestFilter filter, String collField, String field, BiConsumer<RestFilter, T> setter, T value) {
		setter.accept(filter, value);
		Errors errors = validate(filter);
		assertThat(errors.getFieldErrors()).as("field[%s]", field)
				.isNotEmpty()
				.extracting(FieldError::getField)
				.contains(field, collField);
		filter.setField(null);
	}

	private RestFilter filterEqual42() {
		return filter("field", FilterOperation.EQUAL.getCode(), 42);
	}

	@Test
	public void testNot() throws Exception {
		Errors errors;

		RestFilter filter = not(filterEqual42());
		errors = validate(filter);
		assertThat(errors.getFieldErrors()).as("field")
				.isEmpty();
		testCollectionWithOther(filter, "not", "field", RestFilter::setField, "someField");
		testCollectionWithOther(filter, "not", "operation", RestFilter::setOperation, FilterOperation.EQUAL.getCode());
		//noinspection UnnecessaryBoxing
		testCollectionWithOther(filter, "not", "value", RestFilter::setValue, Integer.valueOf(42));
		testCollectionWithOther(filter, "not", "or", RestFilter::setOr, singletonList(filterEqual42()));
		testCollectionWithOther(filter, "not", "and", RestFilter::setAnd, singletonList(filterEqual42()));
	}
}