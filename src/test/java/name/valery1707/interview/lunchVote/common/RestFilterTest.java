package name.valery1707.interview.lunchVote.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RestFilterTest {

	private ObjectMapper mapper;

	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
	}

	@Test
	public void testName() throws Exception {
		String json = ("{\n" +
					   "'and': [\n" +
					   "  {'or': [\n" +
					   "    {'field': 'field1', 'operation': '>', 'value': 1},\n" +
					   "    {'field': 'field2', 'operation': '>', 'value': 1}\n" +
					   "  ]},\n" +
					   "  {'field': 'field3', 'operation': '=', 'value': 42},\n" +
					   "  {'not': {'field': 'field4', 'operation': '=', 'value': 17}}" +
					   "]\n" +
					   "}")
				.replace('\'', '"');
		RestFilter filter = mapper.readValue(json, RestFilter.class);
		assertThat(filter).isNotNull();
		assertThat(filter.getAnd()).isNotNull().hasSize(3);
		assertThat(filter.getAnd().get(0)).isNotNull();
		assertThat(filter.getAnd().get(0).getOr()).isNotNull().hasSize(2);

		assertThat(filter.getAnd().get(0).getOr().get(0)).isNotNull();
		assertThat(filter.getAnd().get(0).getOr().get(0).getField()).isNotNull().isEqualTo("field1");
		assertThat(filter.getAnd().get(0).getOr().get(0).getOperation()).isNotNull().isEqualTo(">");
		assertThat(filter.getAnd().get(0).getOr().get(0).getValue()).isNotNull().isEqualTo(1);

		assertThat(filter.getAnd().get(0).getOr().get(1)).isNotNull();
		assertThat(filter.getAnd().get(0).getOr().get(1).getField()).isNotNull().isEqualTo("field2");
		assertThat(filter.getAnd().get(0).getOr().get(1).getOperation()).isNotNull().isEqualTo(">");
		assertThat(filter.getAnd().get(0).getOr().get(1).getValue()).isNotNull().isEqualTo(1);

		assertThat(filter.getAnd().get(1)).isNotNull();
		assertThat(filter.getAnd().get(1).getField()).isNotNull().isEqualTo("field3");
		assertThat(filter.getAnd().get(1).getOperation()).isNotNull().isEqualTo("=");
		assertThat(filter.getAnd().get(1).getValue()).isNotNull().isEqualTo(42);

		assertThat(filter.getAnd().get(2)).isNotNull();
		assertThat(filter.getAnd().get(2).getNot()).isNotNull();
		assertThat(filter.getAnd().get(2).getNot().getField()).isNotNull().isEqualTo("field4");
		assertThat(filter.getAnd().get(2).getNot().getOperation()).isNotNull().isEqualTo("=");
		assertThat(filter.getAnd().get(2).getNot().getValue()).isNotNull().isEqualTo(17);
	}
}
