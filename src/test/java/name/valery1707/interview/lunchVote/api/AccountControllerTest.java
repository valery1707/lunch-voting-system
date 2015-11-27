package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.Launcher;
import name.valery1707.interview.lunchVote.domain.Account;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Launcher.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AccountControllerTest extends BaseEntityControllerTest {

	@Override
	protected String urlRoot() {
		return "/api/account";
	}

	public static final String ADMIN_ID = "b2c0f756-bc2e-4352-90af-a36f9ab3fb46";
	public static final String USER_1_ID = "e9ad94ae-994a-436e-a99e-dec433af0089";
	public static final String USER_2_ID = "da7be27f-fb01-4d35-9519-537c2500a182";

	@Test
	public void test_10_findAll_unauthorized() throws Exception {
		test_unauthorized(get(urlRoot()));
	}

	@Test
	public void test_10_findAll_asBadUser() throws Exception {
		test_badUser(get(urlRoot()).with(accBadUser()));
	}

	@Test
	public void test_10_findAll_asUser() throws Exception {
		test_forbidden(get(urlRoot()).with(accUser()));
	}

	@Test
	public void test_10_findAll_asAdmin() throws Exception {
		int expectedCount = 3;
		String content = mvc.perform(get(urlRoot()).with(accAdmin()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(expectedCount))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(expectedCount))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(expectedCount)))
				.andExpect(jsonPath("$.content[*].id").value(containsInAnyOrder(ADMIN_ID, USER_1_ID, USER_2_ID)))
				.andExpect(authenticated().withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();

		List<Account> result = jsonToList(Account.class, extractContent(content));
		assertThat(result).hasSize(expectedCount);
		assertThat(result)
				.extracting(Account::getPassword)
				.hasSameSizeAs(result)
				.areExactly(expectedCount, new Condition<>(StringUtils::isNotEmpty, "isNotEmpty"));
	}
}
