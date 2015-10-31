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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
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

	@Test
	public void test_10_findAll_unauthorized() throws Exception {
		mvc.perform(get(URL_ROOT).with(csrf().useInvalidToken()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl(URL_PREFIX + "/login"))
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(URL_ROOT))
		;
	}

	@Test
	public void test_10_findAll_user() throws Exception {
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
		mvc.perform(get(URL_ROOT + "/{id}", UUID.randomUUID().toString()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl(URL_PREFIX + "/login"))
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(URL_ROOT))
		;
	}

	@Test
	public void test_10_findById_notFound() throws Exception {
		mvc.perform(get(URL_ROOT + "/{id}", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()));
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
		mvc.perform(post(URL_ROOT)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
		)
				.andExpect(status().isFound())
				.andExpect(redirectedUrl(URL_PREFIX + "/login"))
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(URL_ROOT))
		;
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
				.andReturn().getResponse().getContentAsString();
		Optional<Restaurant> optional = jsonToList(Restaurant.class, content).stream()
				.filter(r -> !KNOWN_RESTAURANTS.contains(r.getId().toString()))
				.findAny();
		return optional.get();
	}

	@Test
	public void test_40_updateById_unauthorized() throws Exception {
		mvc.perform(put(URL_ROOT + "/{id}", RESTAURANT_MOE_BAR_ID)
						.contentType(CONTENT_TYPE)
						.characterEncoding(ENCODING)
						.content(objectToJson(restaurant("new")))
		)
				.andExpect(status().isFound())
				.andExpect(redirectedUrl(URL_PREFIX + "/login"))
//				.andExpect(status().isUnauthorized())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.message").value("Bad credentials"))
//				.andExpect(jsonPath("$.path").value(URL_ROOT))
		;
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
	public void test_41_patchById() throws Exception {
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
	public void test_50_deleteById_notFound() throws Exception {
		mvc.perform(delete(URL_ROOT + "/{id}", UUID.randomUUID().toString()).with(accAdmin()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()));
	}

	@Test
	public void test_50_deleteById_exists() throws Exception {
		UUID restaurantCreatedId = findRestaurantCreated().getId();

		mvc.perform(delete(URL_ROOT + "/{id}", restaurantCreatedId.toString()).with(accAdmin()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.id").value(restaurantCreatedId.toString()))
		;

		test_10_findAll_user();
	}
}