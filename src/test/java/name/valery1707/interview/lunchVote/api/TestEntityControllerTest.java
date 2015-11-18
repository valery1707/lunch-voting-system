package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.Launcher;
import name.valery1707.interview.lunchVote.domain.TestEntity1;
import name.valery1707.interview.lunchVote.domain.TestEntity2;
import name.valery1707.interview.lunchVote.domain.TestEntity3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Launcher.class)
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
public class TestEntityControllerTest extends BaseEntityControllerTest {
	@Override
	protected String urlRoot() {
		return "/api/test/entity";
	}

	private void assertIncorrect(MockHttpServletRequestBuilder requestBuilder) throws Exception {
		mvc.perform(requestBuilder)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Incorrect filter operation")))
				.andExpect(unauthenticated());
	}

	private void assertNotFound(MockHttpServletRequestBuilder requestBuilder) throws Exception {
		mvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(0))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(0)))
				.andExpect(unauthenticated());
	}

	private void assertFound(MockHttpServletRequestBuilder requestBuilder) throws Exception {
		String content = mvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(unauthenticated())
				.andReturn().getResponse().getContentAsString();

		List<TestEntity1> result = jsonToList(TestEntity1.class, extractContent(content));
		assertThat(result).hasSize(1);
		TestEntity1 entity1 = result.get(0);
		TestEntity2 entity2 = entity1.getSecondLink();

		//region Entity1
		assertThat(entity1.getName()).isEqualTo("1");
		assertThat(entity2).isNotNull();
		assertThat(entity1.getSecondCollection())
				.hasSize(1)
				.containsExactly(entity2)
				.doesNotContainNull();
		//endregion

		//region Entity2
		assertThat(entity2.getName()).isEqualTo("1.1");
		assertThat(entity2.getThirdLink()).isNotNull();
		assertThat(entity2.getThirdCollection())
				.hasSize(4)
				.contains(entity2.getThirdLink())
				.doesNotContainNull();
		//endregion

		//region Entity3
		Set<TestEntity3> entity3s = entity2.getThirdCollection();
		assertThat(entity3s)
				.extracting(TestEntity3::getName)
				.containsOnly("1.1.1", "1.1.2", "1.1.3", "1.1.4");
		//endregion
	}

	@Test
	public void testFindAll() throws Exception {
		assertFound(get(urlRoot()));
	}

	@Test
	public void testFilter_overCollection_byte_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_byte_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_byte_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_byte_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=>;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_byte_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;>;1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_byte_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;1")
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;2")
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;3")
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_byte_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_byte_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_byte_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_byte_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;!~!;0")
		);
	}


	@Test
	public void testFilter_overCollection_short_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_short_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_short_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_short_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=>;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_short_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;>;1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_short_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;1")
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;2")
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;3")
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_short_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_short_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_short_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_short_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_int_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_int_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_int_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_int_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=>;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_int_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;>;1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_int_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;1")
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;2")
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;3")
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_int_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_int_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_int_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_int_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_long_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_long_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_long_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_long_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=>;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_long_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;>;1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_long_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;1")
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;2")
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;3")
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_long_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_long_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_long_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_long_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_double_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<;0.0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<;1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_double_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<=;1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<=;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<=;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_double_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=;1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_double_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=>;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=>;1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=>;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;=>;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_double_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;>;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;>;1.1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;>;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;>;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_double_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;42.42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;1.1")
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;2.2")
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;3.3")
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!=;4.4")
		);
	}

	@Test
	public void testFilter_overCollection_double_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;~;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_double_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!~;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_double_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;~!;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_double_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;!~!;0.0")
		);
	}
}
