package name.valery1707.interview.lunchVote.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import name.valery1707.interview.lunchVote.Launcher;
import name.valery1707.interview.lunchVote.domain.Dish;
import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Launcher.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:application-test.properties")
public class RestaurantControllerTest {

	public static final MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;
	public static final String ENCODING = "UTF-8";
	public static final String URL_PREFIX = "http://localhost";
	public static final String URL_ROOT = "/api/restaurant";

	@Inject
	private WebApplicationContext context;

	private MockMvc mvc;
	private ObjectMapper mapper;

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

	private <T> List<T> jsonToList(Class<T> clazz, String src) throws IOException {
		return mapper.readValue(src, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
	}

	private <T> T jsonToObject(Class<T> clazz, String src) throws IOException {
		return mapper.readValue(src, clazz);
	}

	private <T> String objectToJson(T src) throws IOException {
		return mapper.writeValueAsString(src);
	}

	private static RequestPostProcessor accAdmin() {
		return httpBasic("admin", "admin");
	}

	private static RequestPostProcessor accUser() {
		return httpBasic("user_1", "password one");
	}

	private static RequestPostProcessor accBadUser() {
		return httpBasic("evil", "chaos");
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
//				.andExpect(jsonPath("$.path").value(URL_ROOT))
				;
	}

	protected ResultActions test_badUser(RequestBuilder requestBuilder) throws Exception {
		return mvc.perform(requestBuilder)
				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(startsWith(URL_ROOT)))
				.andExpect(unauthenticated())
				;
	}

	@Test
	public void test_10_findAll_unauthorized() throws Exception {
		test_unauthorized(get(URL_ROOT))
		;
	}

	@Test
	public void test_10_findAll_asBadUser() throws Exception {
		test_badUser(get(URL_ROOT).with(accBadUser()));
	}

	@Test
	public void test_10_findAll_asUser() throws Exception {
		String content = mvc.perform(get(URL_ROOT).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].id").value(containsInAnyOrder(RESTAURANT_MOE_BAR_ID, RESTAURANT_HELL_KITCHEN_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, content);
		assertThat(result).hasSize(2);
	}

	private static final String RESTAURANT_MOE_BAR_ID = "60d4f411-4cff-4f60-b392-46bed14c5f86";
	private static final String RESTAURANT_HELL_KITCHEN_ID = "78a9353f-7e08-40a6-ad70-af2664a37a36";
	private static final Set<String> KNOWN_RESTAURANTS = new HashSet<>(Arrays.asList(RESTAURANT_MOE_BAR_ID, RESTAURANT_HELL_KITCHEN_ID));

	@Test
	public void test_10_findById_unauthorized() throws Exception {
		test_unauthorized(get(URL_ROOT + "/{id}", UUID.randomUUID().toString()))
		;
	}

	@Test
	public void test_10_findById_notFound() throws Exception {
		mvc.perform(get(URL_ROOT + "/{id}", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_10_findById_exists_asBadUser() throws Exception {
		test_badUser(get(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID).with(accBadUser()));
	}

	@Test
	public void test_10_findById_exists() throws Exception {
		String content = mvc.perform(get(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id").value(RESTAURANT_MOE_BAR_ID))
				.andExpect(jsonPath("$.dishes").exists())
				.andExpect(jsonPath("$.dishes").isArray())
				.andExpect(jsonPath("$.dishes", hasSize(2)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Restaurant result = jsonToObject(Restaurant.class, content);
		assertThat(result)
				.isNotNull();
		assertThat(result.getDishes())
				.isNotNull()
				.isNotEmpty()
				.hasSize(2);
	}

	@Test
	public void test_20_create_unauthorized() throws Exception {
		test_unauthorized(post(URL_ROOT)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_20_create_asBadUser() throws Exception {
		test_badUser(post(URL_ROOT)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accBadUser())
		);
	}

	@Test
	public void test_20_create_asUser() throws Exception {
		mvc.perform(post(URL_ROOT)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accUser())
		)
				.andExpect(status().isForbidden())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_20_create_asAdmin() throws Exception {
		Restaurant source = restaurant("Created from Test", dish("dish1", 1.0), dish("dish 2", 2.0));
		String content = mvc.perform(post(URL_ROOT)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(source))
						.with(accAdmin())
		)
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(redirectedUrlPattern(URL_PREFIX + URL_ROOT + "/*"))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id", not(RESTAURANT_MOE_BAR_ID)))
				.andExpect(jsonPath("$.id", not(source.getId())))
				.andExpect(authenticated().withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();
		Restaurant result = jsonToObject(Restaurant.class, content);
		assertThat(result.getName()).isEqualTo(source.getName());
		assertThat(result.getDishes()).hasSameSizeAs(source.getDishes());


		content = mvc.perform(get(URL_ROOT + "/{id}", result.getId()).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id").value(result.getId().toString()))
				.andExpect(jsonPath("$.dishes").exists())
				.andExpect(jsonPath("$.dishes").isArray())
				.andExpect(jsonPath("$.dishes", hasSize(result.getDishes().size())))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		Restaurant saved = jsonToObject(Restaurant.class, content);
		assertThat(saved.getName()).isEqualTo(result.getName());
		assertThat(saved.getDishes())
				.hasSameSizeAs(result.getDishes())
				.containsOnlyElementsOf(result.getDishes());
	}

	private static Restaurant restaurant(String name, Dish... dishes) {
		Restaurant restaurant = new Restaurant();
		restaurant.setRandomId();
		restaurant.setName(name);
		for (Dish dish : dishes) {
			dish.setRestaurant(restaurant);
			restaurant.getDishes().add(dish);
		}
		return restaurant;
	}

	private static Dish dish(String name, Double price) {
		Dish dish = new Dish();
		dish.setName(name);
		dish.setPrice(price);
		return dish;
	}

	private Restaurant findRestaurantCreated() throws Exception {
		String content = mvc.perform(get(URL_ROOT).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
		Optional<Restaurant> optional = jsonToList(Restaurant.class, content).stream()
				.filter(r -> !KNOWN_RESTAURANTS.contains(r.getId().toString()))
				.findAny();
		return optional.get();
	}

	@Test
	public void test_40_updateById_unauthorized() throws Exception {
		test_unauthorized(put(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_40_updateById_asBadUser() throws Exception {
		test_badUser(put(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accBadUser())
		);
	}

	@Test
	public void test_40_updateById_asUser() throws Exception {
		mvc.perform(put(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accUser())
		)
				.andExpect(status().isForbidden())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_41_updateById_asAdmin_notFound() throws Exception {
		mvc.perform(put(URL_ROOT + "/{id}", UUID.randomUUID().toString())
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accAdmin())
		)
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("ADMIN"))
		;
	}

	@Test
	public void test_40_updateById_asAdmin() throws Exception {
		//Read original value
		Restaurant created = findRestaurantCreated();

		//Read value for update
		Restaurant updateSource = findRestaurantCreated();
		//Make modification
		updateSource.setName(updateSource.getName() + " {update}");
		Dish removedDish = updateSource.getDishes().iterator().next();
		updateSource.getDishes().remove(removedDish);
		Dish savedDish = updateSource.getDishes().iterator().next();
		Dish newDish = dish("from update", 40.0);
		updateSource.getDishes().add(newDish);

		//Send modification
		mvc.perform(put(URL_ROOT + "/{id}", created.getId().toString())
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(updateSource))
						.with(accAdmin())
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").value(created.getId().toString()))
				.andExpect(jsonPath("$.dishes").isArray())
				.andExpect(jsonPath("$.dishes", hasSize(updateSource.getDishes().size())))
				.andExpect(authenticated().withRoles("ADMIN"))
		;

		//Read actual value
		Restaurant updateResult = findRestaurantCreated();
		assertThat(updateResult.getName())
				.isEqualTo(updateSource.getName())
				.isNotEqualTo(created.getName());
		assertThat(updateResult.getDishes())
				.contains(savedDish)
				.doesNotContain(removedDish);
		//New dish list contains newDish
		assertTrue("newDish", updateResult.getDishes().stream()
						.filter(dish -> !savedDish.equals(dish))
						.allMatch(dish ->
										dish.getName().equals(newDish.getName()) && dish.getPrice().equals(newDish.getPrice())
						)
		);
	}

	@Test
	public void test_41_patchById_unauthorized() throws Exception {
		test_unauthorized(patch(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_41_patchById_asBadUser() throws Exception {
		test_badUser(patch(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accBadUser())
		);
	}

	@Test
	public void test_41_patchById_asUser() throws Exception {
		mvc.perform(patch(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accUser())
		)
				.andExpect(status().isForbidden())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_41_patchById_asAdmin_notFound() throws Exception {
		mvc.perform(patch(URL_ROOT + "/{id}", UUID.randomUUID().toString())
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
						.with(accAdmin())
		)
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("ADMIN"))
		;
	}

	@Test
	public void test_41_patchById_asAdmin() throws Exception {
		//Read original value
		Restaurant created = findRestaurantCreated();

		//Read value for update
		Restaurant updateSource = findRestaurantCreated();
		//Make modification
		updateSource.setName(updateSource.getName() + " {patch}");
		Dish newDish = dish("from patch", 40.1);
		updateSource.getDishes().clear();
		updateSource.getDishes().add(newDish);

		//Send modification
		mvc.perform(patch(URL_ROOT + "/{id}", created.getId().toString())
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(updateSource))
						.with(accAdmin())
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").value(created.getId().toString()))
				.andExpect(jsonPath("$.dishes").isArray())
				.andExpect(jsonPath("$.dishes", hasSize(created.getDishes().size() + updateSource.getDishes().size())))
				.andExpect(authenticated().withRoles("ADMIN"))
		;

		//Read actual value
		Restaurant updateResult = findRestaurantCreated();
		assertThat(updateResult.getName())
				.isEqualTo(updateSource.getName())
				.isNotEqualTo(created.getName());
		assertThat(updateResult.getDishes())
				.containsAll(created.getDishes());
		//New dish list contains newDish
		assertTrue("newDish", updateResult.getDishes().stream()
						.filter(dish -> !created.getDishes().contains(dish))
						.allMatch(dish ->
										dish.getName().equals(newDish.getName()) && dish.getPrice().equals(newDish.getPrice())
						)
		);
	}

	@Test
	public void test_50_deleteById_unauthorized() throws Exception {
		test_unauthorized(delete(URL_ROOT + "/{id}", UUID.randomUUID().toString()))
		;
	}

	@Test
	public void test_50_deleteById_asUser() throws Exception {
		mvc.perform(delete(URL_ROOT + "/{id}", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isForbidden())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_50_deleteById_asAdmin_notFound() throws Exception {
		mvc.perform(delete(URL_ROOT + "/{id}", UUID.randomUUID().toString()).with(accAdmin()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("ADMIN"))
		;
	}

	@Test
	public void test_50_deleteById_asAdmin_exists() throws Exception {
		UUID restaurantCreatedId = findRestaurantCreated().getId();

		mvc.perform(delete(URL_ROOT + "/{id}", restaurantCreatedId.toString()).with(accAdmin()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id").value(restaurantCreatedId.toString()))
				.andExpect(authenticated().withRoles("ADMIN"))
		;

		test_10_findAll_asUser();
	}

	@Test
	public void test_60_voteScore_unauthorized() throws Exception {
		test_unauthorized(get(URL_ROOT + "/vote"))
		;
	}

	@Test
	public void test_60_voteScore_asBadUser() throws Exception {
		test_badUser(get(URL_ROOT + "/vote").with(accBadUser()));
	}

	@Test
	public void test_60_voteScore_malformedDate() throws Exception {
		mvc.perform(get(URL_ROOT + "/vote").with(accUser()).param("date", "2015.10.21"))
				.andExpect(status().isBadRequest())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.path").value(URL_ROOT + "/vote"))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_60_voteScore_now() throws Exception {
		mvc.perform(get(URL_ROOT + "/vote").with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[*].restaurantId").value(containsInAnyOrder(RESTAURANT_HELL_KITCHEN_ID)))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_60_voteScore_marty() throws Exception {
		mvc.perform(get(URL_ROOT + "/vote").with(accUser()).param("date", "2015-10-21"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(0)))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_unauthorized() throws Exception {
		test_unauthorized(post(URL_ROOT + "/{id}/vote", RESTAURANT_MOE_BAR_ID))
		;
	}

	@Test
	public void test_61_vote_asBadUser() throws Exception {
		test_badUser(post(URL_ROOT + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accBadUser()));
	}

	@Test
	public void test_61_vote_malformedDate() throws Exception {
		mvc.perform(post(URL_ROOT + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()).param("datetime", "2015.10.21"))
				.andExpect(status().isBadRequest())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.path").value(URL_ROOT + "/vote"))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_notFound() throws Exception {
		mvc.perform(post(URL_ROOT + "/{id}/vote", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_exists_now() throws Exception {
		mvc.perform(post(URL_ROOT + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_MOE_BAR_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_exists_doc() throws Exception {
		//First try
		mvc.perform(post(URL_ROOT + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()).param("datetime", "1885-09-02T10:30:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_MOE_BAR_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
		//Second try
		mvc.perform(post(URL_ROOT + "/{id}/vote", RESTAURANT_HELL_KITCHEN_ID).with(accUser()).param("datetime", "1885-09-02T10:40:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isAccepted())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_HELL_KITCHEN_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
		//After max hour
		mvc.perform(post(URL_ROOT + "/{id}/vote", RESTAURANT_HELL_KITCHEN_ID).with(accUser()).param("datetime", "1885-09-02T11:00:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isNotModified())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_HELL_KITCHEN_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
	}
}
