package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.Launcher;
import name.valery1707.interview.lunchVote.domain.Dish;
import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Launcher.class)
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "classpath:application-test.properties")
public class RestaurantControllerTest extends BaseEntityControllerTest {

	@Override
	protected String urlRoot() {
		return "/api/restaurant";
	}

	@Test
	public void test_10_findAll_unauthorized() throws Exception {
		test_unauthorized(get(urlRoot()))
		;
	}

	@Test
	public void test_10_findAll_asBadUser() throws Exception {
		test_badUser(get(urlRoot()).with(accBadUser()));
	}

	@Test
	public void test_10_findAll_asUser() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(2))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[*].id").value(containsInAnyOrder(RESTAURANT_MOE_BAR_ID, RESTAURANT_HELL_KITCHEN_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(2);
	}

	@Test
	public void test_10_findAll_asUser_paged() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("size", "1")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(false))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(1))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[*].id").value(anyOf(hasItem(RESTAURANT_MOE_BAR_ID), hasItem(RESTAURANT_HELL_KITCHEN_ID))))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(1);
	}

	@Test
	public void test_10_findAll_asUser_sortedByDirectFields() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("sort", "name,ASC")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(2))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[*].id").value(contains(RESTAURANT_HELL_KITCHEN_ID, RESTAURANT_MOE_BAR_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(2);
	}

	@Ignore("With sorting by nested fields repository will return not distinct root entities.")
	@Test
	public void test_10_findAll_asUser_sortedByNestedFields() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("sort", "name,ASC")
				.param("sort", "dishes.name,dishes.price,DESC")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(2))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[*].id").value(contains(RESTAURANT_HELL_KITCHEN_ID, RESTAURANT_MOE_BAR_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(2);
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_direct_incorrectFilter() throws Exception {
		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "name")
		)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Incorrect filter format")))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_direct_unknownField() throws Exception {
		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "description;~;ha-ha")
		)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Unknown field [description]")))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_direct_string_notFound() throws Exception {
		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "name;~;heLL")
				.param("filter", "name;~;bar")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(0))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(0))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(0)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_direct_string_found() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "name;~;heLL")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(1))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[*].id").value(contains(RESTAURANT_HELL_KITCHEN_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(1);
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_nested_unknownField() throws Exception {
		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "name.description;~;ha-ha")
		)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Unknown field [description]")))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "dishes.description;~;ha-ha")
		)
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.timestamp").isNumber())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.message").isString())
				.andExpect(jsonPath("$.message").value(containsString("Unknown field [description]")))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_nested_string_notFound() throws Exception {
		mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "dishes.name;~;bread")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(0))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(0))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(0)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_nested_string_found() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "dishes.name;~;beer")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(1))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[*].id").value(contains(RESTAURANT_MOE_BAR_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(1);
	}

	@Test
	public void test_10_findAll_asUser_simpleFilter_nested_number_found() throws Exception {
		String content = mvc.perform(get(urlRoot()).with(accUser())
				.param("filter", "dishes.price;<;4")
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(1))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[*].id").value(contains(RESTAURANT_MOE_BAR_ID)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();

		List<Restaurant> result = jsonToList(Restaurant.class, extractContent(content));
		assertThat(result).hasSize(1);
	}

	private static final String RESTAURANT_MOE_BAR_ID = "60d4f411-4cff-4f60-b392-46bed14c5f86";
	private static final String RESTAURANT_HELL_KITCHEN_ID = "78a9353f-7e08-40a6-ad70-af2664a37a36";
	private static final Set<String> KNOWN_RESTAURANTS = new HashSet<>(Arrays.asList(RESTAURANT_MOE_BAR_ID, RESTAURANT_HELL_KITCHEN_ID));

	@Test
	public void test_10_findById_unauthorized() throws Exception {
		test_unauthorized(get(urlRoot() + "/{id}", UUID.randomUUID().toString()))
		;
	}

	@Test
	public void test_10_findById_notFound() throws Exception {
		mvc.perform(get(urlRoot() + "/{id}", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_10_findById_exists_asBadUser() throws Exception {
		test_badUser(get(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID).with(accBadUser()));
	}

	@Test
	public void test_10_findById_exists() throws Exception {
		String content = mvc.perform(get(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID).with(accUser()))
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
		test_unauthorized(post(urlRoot())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_20_create_asBadUser() throws Exception {
		test_badUser(post(urlRoot())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
				.with(accBadUser())
		);
	}

	@Test
	public void test_20_create_asUser() throws Exception {
		mvc.perform(post(urlRoot())
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
		String content = mvc.perform(post(urlRoot())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(source))
				.with(accAdmin())
		)
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(redirectedUrlPattern(URL_PREFIX + urlRoot() + "/*"))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.result").exists())
				.andExpect(jsonPath("$.result").isMap())
				.andExpect(jsonPath("$.result.id").exists())
				.andExpect(jsonPath("$.result.id", not(RESTAURANT_MOE_BAR_ID)))
				.andExpect(jsonPath("$.result.id", not(source.getId())))
				.andExpect(authenticated().withRoles("ADMIN"))
				.andReturn().getResponse().getContentAsString();
		Restaurant result = jsonToObject(Restaurant.class, extractResult(content));
		assertThat(result.getName()).isEqualTo(source.getName());
		assertThat(result.getDishes()).hasSameSizeAs(source.getDishes());


		content = mvc.perform(get(urlRoot() + "/{id}", result.getId()).with(accUser()))
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

	@Test
	public void test_20_create_asAdmin_withBugs() throws Exception {
		//Without name in Restaurant
		mvc.perform(post(urlRoot())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant(null, dish("dish1", 1.0), dish("dish 2", 2.0))))
				.with(accAdmin())
		)
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.valid").exists())
				.andExpect(jsonPath("$.valid").isBoolean())
				.andExpect(jsonPath("$.valid").value(false))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("name")))
				.andExpect(authenticated().withRoles("ADMIN"));

		//Without price in Dish
		mvc.perform(post(urlRoot())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("Created from Test", dish("dish1", null), dish("dish 2", 2.0))))
				.with(accAdmin())
		)
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.valid").exists())
				.andExpect(jsonPath("$.valid").isBoolean())
				.andExpect(jsonPath("$.valid").value(false))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("dishes[].price")))
				.andExpect(authenticated().withRoles("ADMIN"));
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
		String content = mvc.perform(get(urlRoot()).with(accUser()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$.first").isBoolean())
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").isBoolean())
				.andExpect(jsonPath("$.last").value(true))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").isNumber())
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.size").isNumber())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.numberOfElements").isNumber())
				.andExpect(jsonPath("$.numberOfElements").value(3))
				.andExpect(jsonPath("$.content").exists())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(3)))
				.andExpect(authenticated().withRoles("USER"))
				.andReturn().getResponse().getContentAsString();
		Optional<Restaurant> optional = jsonToList(Restaurant.class, extractContent(content)).stream()
				.filter(r -> !KNOWN_RESTAURANTS.contains(r.getId().toString()))
				.findAny();
		return optional.get();
	}

	@Test
	public void test_40_updateById_unauthorized() throws Exception {
		test_unauthorized(put(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_40_updateById_asBadUser() throws Exception {
		test_badUser(put(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
				.with(accBadUser())
		);
	}

	@Test
	public void test_40_updateById_asUser() throws Exception {
		mvc.perform(put(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
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
	public void test_40_updateById_asAdmin_notFound() throws Exception {
		mvc.perform(put(urlRoot() + "/{id}", UUID.randomUUID().toString())
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
		mvc.perform(put(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(updateSource))
				.with(accAdmin())
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.result").exists())
				.andExpect(jsonPath("$.result").isMap())
				.andExpect(jsonPath("$.result.id").value(created.getId().toString()))
				.andExpect(jsonPath("$.result.dishes").isArray())
				.andExpect(jsonPath("$.result.dishes", hasSize(updateSource.getDishes().size())))
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
	public void test_40_updateById_asAdmin_withBugs() throws Exception {
		Restaurant created;

		//Without name in Restaurant
		created = findRestaurantCreated();
		created.setName(null);
		mvc.perform(put(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(created))
				.with(accAdmin())
		)
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.valid").exists())
				.andExpect(jsonPath("$.valid").isBoolean())
				.andExpect(jsonPath("$.valid").value(false))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("name")))
				.andExpect(authenticated().withRoles("ADMIN"));

		//Without price in Dish
		created = findRestaurantCreated();
		created.getDishes().iterator().next().setPrice(null);
		mvc.perform(put(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(created))
				.with(accAdmin())
		)
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.valid").exists())
				.andExpect(jsonPath("$.valid").isBoolean())
				.andExpect(jsonPath("$.valid").value(false))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("dishes[].price")))
				.andExpect(authenticated().withRoles("ADMIN"));

		//Without name in Dish
		created = findRestaurantCreated();
		created.getDishes().iterator().next().setName(null);
		mvc.perform(put(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(created))
				.with(accAdmin())
		)
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.valid").exists())
				.andExpect(jsonPath("$.valid").isBoolean())
				.andExpect(jsonPath("$.valid").value(false))
				.andExpect(jsonPath("$.errors").exists())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("dishes[].name")))
				.andExpect(authenticated().withRoles("ADMIN"));
	}

	@Test
	public void test_41_patchById_unauthorized() throws Exception {
		test_unauthorized(patch(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
		)
		;
	}

	@Test
	public void test_41_patchById_asBadUser() throws Exception {
		test_badUser(patch(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(restaurant("new")))
				.with(accBadUser())
		);
	}

	@Test
	public void test_41_patchById_asUser() throws Exception {
		mvc.perform(patch(urlRoot() + "/{id}", RESTAURANT_MOE_BAR_ID)
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
		mvc.perform(patch(urlRoot() + "/{id}", UUID.randomUUID().toString())
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
		mvc.perform(patch(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(updateSource))
				.with(accAdmin())
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.result").exists())
				.andExpect(jsonPath("$.result").isMap())
				.andExpect(jsonPath("$.result.id").value(created.getId().toString()))
				.andExpect(jsonPath("$.result.dishes").isArray())
				.andExpect(jsonPath("$.result.dishes", hasSize(created.getDishes().size() + updateSource.getDishes().size())))
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
	public void test_41_patchById_asAdmin_partial() throws Exception {
		//Read original value
		Restaurant created = findRestaurantCreated();

		//Read value for update
		Restaurant updateSource = findRestaurantCreated();
		//Make modification
		updateSource.setName(null);//Do not send name
		Iterator<Dish> iterator = updateSource.getDishes().iterator();
		//Patch only dish price
		if (iterator.hasNext()) {
			Dish dish = iterator.next();
			dish.setName(null);//Do not send name
			dish.setPrice(dish.getPrice() * 2);
		}
		//Patch only dish name
		if (iterator.hasNext()) {
			Dish dish = iterator.next();
			dish.setName(dish.getName() + " {partially patched}");
			dish.setPrice(null);//Do not send price
		}
		//Add new dish
		Dish newDish = dish("from partial patch", 142.1);
		updateSource.getDishes().add(newDish);
		//Send dish with ID generated on client side
		Dish dishWithIncorrectId = dish("dish with id from client", 10.0);
		dishWithIncorrectId.setRandomId();
		updateSource.getDishes().add(dishWithIncorrectId);
		//Send dish with ID generated on client side
		Dish partialDishWithIncorrectId = dish("partial dish with id from client", null);
		partialDishWithIncorrectId.setRandomId();
		updateSource.getDishes().add(partialDishWithIncorrectId);

		//Send modification
		mvc.perform(patch(urlRoot() + "/{id}", created.getId().toString())
				.contentType(CONTENT_TYPE)
				.characterEncoding(ENCODING)
				.content(objectToJson(updateSource))
				.with(accAdmin())
		)
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.result").exists())
				.andExpect(jsonPath("$.result").isMap())
				.andExpect(jsonPath("$.result.id").value(created.getId().toString()))
				.andExpect(jsonPath("$.result.dishes").isArray())
				.andExpect(jsonPath("$.result.dishes", hasSize(updateSource.getDishes().size() - 2)))
				.andExpect(authenticated().withRoles("ADMIN"))
		;

		//Read actual value
		Restaurant updateResult = findRestaurantCreated();
		assertThat(updateResult.getName())
				.isEqualTo(created.getName());
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
		test_unauthorized(delete(urlRoot() + "/{id}", UUID.randomUUID().toString()))
		;
	}

	@Test
	public void test_50_deleteById_asUser() throws Exception {
		mvc.perform(delete(urlRoot() + "/{id}", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isForbidden())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_50_deleteById_asAdmin_notFound() throws Exception {
		mvc.perform(delete(urlRoot() + "/{id}", UUID.randomUUID().toString()).with(accAdmin()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("ADMIN"))
		;
	}

	@Test
	public void test_50_deleteById_asAdmin_exists() throws Exception {
		UUID restaurantCreatedId = findRestaurantCreated().getId();

		mvc.perform(delete(urlRoot() + "/{id}", restaurantCreatedId.toString()).with(accAdmin()))
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
		test_unauthorized(get(urlRoot() + "/vote"))
		;
	}

	@Test
	public void test_60_voteScore_asBadUser() throws Exception {
		test_badUser(get(urlRoot() + "/vote").with(accBadUser()));
	}

	@Test
	public void test_60_voteScore_malformedDate() throws Exception {
		mvc.perform(get(urlRoot() + "/vote").with(accUser()).param("date", "2015.10.21"))
				.andExpect(status().isBadRequest())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.path").value(urlRoot() + "/vote"))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_60_voteScore_now() throws Exception {
		mvc.perform(get(urlRoot() + "/vote").with(accUser()))
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
		mvc.perform(get(urlRoot() + "/vote").with(accUser()).param("date", "2015-10-21"))
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
		test_unauthorized(post(urlRoot() + "/{id}/vote", RESTAURANT_MOE_BAR_ID))
		;
	}

	@Test
	public void test_61_vote_asBadUser() throws Exception {
		test_badUser(post(urlRoot() + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accBadUser()));
	}

	@Test
	public void test_61_vote_malformedDate() throws Exception {
		mvc.perform(post(urlRoot() + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()).param("datetime", "2015.10.21"))
				.andExpect(status().isBadRequest())
//				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
//				.andExpect(content().encoding(ENCODING))
//				.andExpect(jsonPath("$").isMap())
//				.andExpect(jsonPath("$.path").value(urlRoot() + "/vote"))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_notFound() throws Exception {
		mvc.perform(post(urlRoot() + "/{id}/vote", UUID.randomUUID().toString()).with(accUser()))
				.andExpect(status().isNotFound())
				.andExpect(content().string(isEmptyOrNullString()))
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_61_vote_exists_now() throws Exception {
		mvc.perform(post(urlRoot() + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()))
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
		mvc.perform(post(urlRoot() + "/{id}/vote", RESTAURANT_MOE_BAR_ID).with(accUser()).param("datetime", "1885-09-02T10:30:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_MOE_BAR_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
		//Second try
		mvc.perform(post(urlRoot() + "/{id}/vote", RESTAURANT_HELL_KITCHEN_ID).with(accUser()).param("datetime", "1885-09-02T10:40:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isAccepted())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_HELL_KITCHEN_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
		//After max hour
		mvc.perform(post(urlRoot() + "/{id}/vote", RESTAURANT_HELL_KITCHEN_ID).with(accUser()).param("datetime", "1885-09-02T11:00:21.000Z[" + ZoneId.systemDefault().toString() + "]"))
				.andExpect(status().isNotModified())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(RESTAURANT_HELL_KITCHEN_ID))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;
	}

	@Test
	public void test_65_removeRestaurant_withVote() throws Exception {
		//Create new Restaurant
		test_20_create_asAdmin();
		Restaurant created = findRestaurantCreated();
		String createdId = created.getId().toString();

		//Vote for him
		mvc.perform(post(urlRoot() + "/{id}/vote", createdId).with(accUser2()))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.restaurantId").value(createdId))
				.andExpect(jsonPath("$.dateTime").isNumber())
				.andExpect(authenticated().withRoles("USER"))
		;

		//Check vote is exists
		mvc.perform(get(urlRoot() + "/vote").with(accUser2()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(CONTENT_TYPE))
				.andExpect(content().encoding(ENCODING))
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$[*].restaurantId").value(hasItem(createdId)))
				.andExpect(authenticated().withRoles("USER"))
		;

		//Delete restaurant
		test_50_deleteById_asAdmin_exists();
	}
}
