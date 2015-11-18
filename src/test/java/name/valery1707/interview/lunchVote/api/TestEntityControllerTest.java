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
import org.springframework.web.util.NestedServletException;

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

	private void assertIncorrectValue(MockHttpServletRequestBuilder requestBuilder) throws Exception {
		mvc.perform(requestBuilder)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Incorrect filter value")))
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
	public void testFilter_overCollection_collection_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_entity_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdLink;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_entity_isNull() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdLink;_;")
		);
	}

	@Test
	public void testFilter_overCollection_entity_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdLink;!_;")
		);
	}

	//region byte

	@Test
	public void testFilter_overCollection_byte_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveByte;<;***")
		);
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

	//endregion
	//region short

	@Test
	public void testFilter_overCollection_short_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveShort;<;***")
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

	//endregion
	//region int

	@Test
	public void testFilter_overCollection_int_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveInt;<;***")
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

	//endregion
	//region long

	@Test
	public void testFilter_overCollection_long_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveLong;<;***")
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

	//endregion
	//region double

	@Test
	public void testFilter_overCollection_double_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveDouble;<;***")
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

	//endregion
	//region boolean

	@Test
	public void testFilter_overCollection_boolean_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_lessThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;<;true")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;<;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_lessThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;<=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;<=;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_equal() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;=;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;=>;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;=>;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_greaterThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;>;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;>;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!=;false")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!=;true")
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!=;false")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;~;true")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!~;true")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;~!;true")
		);
	}

	@Test
	public void testFilter_overCollection_boolean_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.primitiveBoolean;!~!;true")
		);
	}

	//endregion

	//region Byte

	@Test
	public void testFilter_overCollection_Byte_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=>;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;>;3")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;3")
				.param("filter", "secondCollection.thirdCollection.objectByte;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Byte_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectByte;!_;")
		);
	}

	//endregion
	//region Short

	@Test
	public void testFilter_overCollection_Short_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Short_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_Short_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Short_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Short_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=>;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Short_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;>;3")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Short_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;3")
				.param("filter", "secondCollection.thirdCollection.objectShort;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_Short_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Short_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Short_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Short_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Short_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Short_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectShort;!_;")
		);
	}

	//endregion
	//region Int

	@Test
	public void testFilter_overCollection_Int_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Int_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_Int_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Int_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Int_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=>;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Int_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;>;3")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Int_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;3")
				.param("filter", "secondCollection.thirdCollection.objectInt;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_Int_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Int_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Int_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Int_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Int_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Int_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectInt;!_;")
		);
	}

	//endregion
	//region Long

	@Test
	public void testFilter_overCollection_Long_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Long_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<;0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<;42")
		);
	}

	@Test
	public void testFilter_overCollection_Long_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;<=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Long_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=;42")
		);
	}

	@Test
	public void testFilter_overCollection_Long_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=>;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;=>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Long_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;>;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;>;3")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;>;4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;>;42")
		);
	}

	@Test
	public void testFilter_overCollection_Long_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;3")
				.param("filter", "secondCollection.thirdCollection.objectLong;!=;4")
		);
	}

	@Test
	public void testFilter_overCollection_Long_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Long_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!~;0")
		);
	}

	@Test
	public void testFilter_overCollection_Long_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Long_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!~!;0")
		);
	}

	@Test
	public void testFilter_overCollection_Long_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Long_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectLong;!_;")
		);
	}

	//endregion
	//region Double

	@Test
	public void testFilter_overCollection_Double_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Double_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<;0.0")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<;3.3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_Double_lessThanOrEqualTo() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<=;3.3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<=;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;<=;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_Double_equal() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=;3.3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_Double_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=>;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=>;3.3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=>;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;=>;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_Double_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;>;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;>;3.3")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;>;4.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;>;42.42")
		);
	}

	@Test
	public void testFilter_overCollection_Double_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;0.0")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;3.3")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;4.4")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;42.42")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;3.3")
				.param("filter", "secondCollection.thirdCollection.objectDouble;!=;4.4")
		);
	}

	@Test
	public void testFilter_overCollection_Double_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;~;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_Double_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!~;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_Double_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;~!;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_Double_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!~!;0.0")
		);
	}

	@Test
	public void testFilter_overCollection_Double_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Double_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectDouble;!_;")
		);
	}

	//endregion
	//region Boolean

	@Test
	public void testFilter_overCollection_Boolean_incorrectValue() throws Exception {
		assertIncorrectValue(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;<;***")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_lessThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;<;true")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;<;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_lessThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;<=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;<=;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_equal() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;=;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;=>;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;=>;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_greaterThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;>;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;>;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!=;true")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!=;false")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!=;true")
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!=;false")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_like() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;~;true")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_notLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!~;true")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_caseSensitiveLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;~!;true")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_caseSensitiveNotLike() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!~!;true")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_isNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;_;")
		);
	}

	@Test
	public void testFilter_overCollection_Boolean_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.objectBoolean;!_;")
		);
	}

	//endregion
	//region String

	@Test
	public void testFilter_overCollection_String_lessThan() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;<;1.1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;<;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_lessThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;<=;1.1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;<=;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_equal() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;=;1.1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;=;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_greaterThanOrEqualTo() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;=>;1.1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;=>;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_greaterThan() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;>;1.1.1")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;>;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_notEqual() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.1")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.4")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.1")
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.2")
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.3")
				.param("filter", "secondCollection.thirdCollection.name;!=;1.1.4")
		);
	}

	@Test
	public void testFilter_overCollection_String_like() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;.1.")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;1.1.")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;1.1.?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;1.1.*")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;1.1.1?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~;1.1.1*")
		);
	}

	@Test
	public void testFilter_overCollection_String_notLike() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;.1.")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.?")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.*")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.1?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.1*")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~;1.1.*")
		);
	}

	@Test
	public void testFilter_overCollection_String_caseSensitiveLike() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;.1.")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;1.1.")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;1.1.?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;1.1.*")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;1.1.1?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;~!;1.1.1*")
		);
	}

	@Test
	public void testFilter_overCollection_String_caseSensitiveNotLike() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;.1.")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.?")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.*")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.1?")
		);
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.1*")
		);
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!~!;1.1.*")
		);
	}

	@Test
	public void testFilter_overCollection_String_isNull() throws Exception {
		assertNotFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;_;")
		);
	}

	@Test
	public void testFilter_overCollection_String_isNotNull() throws Exception {
		assertFound(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;!_;")
		);
	}

	@Test(expected = NestedServletException.class)
	public void testFilter_overCollection_String_unknownOperation() throws Exception {
		assertIncorrect(get(urlRoot())
				.param("filter", "secondCollection.thirdCollection.name;?;***")
		);
	}

	//endregion
}
