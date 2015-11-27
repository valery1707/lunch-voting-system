package name.valery1707.interview.lunchVote.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseEntityControllerTest {
	public static final MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
	public static final String ENCODING = "UTF-8";
	public static final String URL_PREFIX = "http://localhost";

	@Inject
	private WebApplicationContext context;

	protected MockMvc mvc;
	private ObjectMapper mapper;

	protected abstract String urlRoot();

	protected RequestPostProcessor accAdmin() {
		return httpBasic("admin", "admin");
	}

	protected RequestPostProcessor accUser() {
		return httpBasic("user_1", "password one");
	}

	protected RequestPostProcessor accUser2() {
		return httpBasic("user_2", "password two");
	}

	protected RequestPostProcessor accBadUser() {
		return httpBasic("evil", "chaos");
	}

	@Before
	public void setUp() {
		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(springSecurity())
				.defaultRequest(get("/").with(csrf()))
				.build();
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	protected <T> List<T> jsonToList(Class<T> clazz, String src) throws IOException {
		return mapper.readValue(src, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
	}

	protected <T> T jsonToObject(Class<T> clazz, String src) throws IOException {
		return mapper.readValue(src, clazz);
	}

	protected <T> String objectToJson(T src) throws IOException {
		return mapper.writeValueAsString(src);
	}

	private String jsonExtract(String jsonPath, String src) throws IOException {
		Object obj = JsonPath.read(src, jsonPath);
		return objectToJson(obj);
	}

	protected String extractResult(String src) throws IOException {
		return jsonExtract("$.result", src);
	}

	protected String extractContent(String src) throws IOException {
		return jsonExtract("$.content", src);
	}

	protected ResultActions test_unauthorized(RequestBuilder requestBuilder) throws Exception {
		return mvc.perform(requestBuilder)
				.andExpect(status().isFound())
				.andExpect(redirectedUrl(URL_PREFIX + "/login"))
				.andExpect(unauthenticated())
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(urlRoot()))
				;
	}

	protected ResultActions test_badUser(RequestBuilder requestBuilder) throws Exception {
		return mvc.perform(requestBuilder)
				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(startsWith(urlRoot())))
				.andExpect(unauthenticated())
				;
	}

	protected ResultActions test_forbidden(RequestBuilder requestBuilder) throws Exception {
		return mvc.perform(requestBuilder)
				.andExpect(status().isForbidden())
				.andExpect(authenticated())
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(urlRoot()))
				;
	}
}
