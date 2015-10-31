package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.Launcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static name.valery1707.interview.lunchVote.api.RestaurantControllerTest.CONTENT_TYPE;
import static name.valery1707.interview.lunchVote.api.RestaurantControllerTest.ENCODING;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Launcher.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:application-test.properties")
public class StatusControllerTest {
	public static final String URL_ROOT = "/status/database";

	@Inject
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(springSecurity())
				.defaultRequest(get("/").with(csrf()))
				.build();
	}

	@Test
	public void testDatabase() throws Exception {
		mvc.perform(get(URL_ROOT))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.version").isString())
				.andExpect(jsonPath("$.databaseName").isString())
				.andExpect(jsonPath("$.username").isString())
				.andExpect(jsonPath("$.sessionId").isNumber())
				.andExpect(jsonPath("$.memoryUsed").isNumber())
				.andExpect(jsonPath("$.lastAppliedMigration").isString())
				.andExpect(unauthenticated())
		;
	}
}