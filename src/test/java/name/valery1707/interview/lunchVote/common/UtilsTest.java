package name.valery1707.interview.lunchVote.common;

import org.junit.Test;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

	@Test
	public void testIterableToList() throws Exception {
		List<String> source = Arrays.asList("1", "2");
		Iterable<String> iterable = new PageImpl<>(source);
		List<String> result = Utils.iterableToList(iterable);
		assertThat(result)
				.doesNotHaveSameClassAs(iterable)
				.containsExactlyElementsOf(source);
	}

	@Test
	public void testListToList() throws Exception {
		List<String> source = Arrays.asList("1", "2");
		List<String> result = Utils.iterableToList(source);
		assertThat(result)
				.isSameAs(source)
				.containsExactlyElementsOf(source);
	}

	@Test
	public void testSetToList() throws Exception {
		List<String> source = Arrays.asList("1", "2");
		List<String> result = Utils.iterableToList(new HashSet<>(source));
		assertThat(result)
				.doesNotHaveSameClassAs(source)
				.isNotSameAs(source)
				.containsExactlyElementsOf(source);
	}

	@Test(expected = IllegalAccessException.class)
	public void testConstructor() throws Exception {
		Utils utils = new Utils();
	}
}