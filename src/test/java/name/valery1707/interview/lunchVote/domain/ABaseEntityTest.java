package name.valery1707.interview.lunchVote.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ABaseEntityTest {

	@Test
	public void testEquals() throws Exception {
		Restaurant restaurant1 = new Restaurant();
		Restaurant restaurant2 = new Restaurant();
		assertNotEquals(restaurant1, null);
		assertEquals(restaurant1, restaurant1);
		assertNotEquals(restaurant1, restaurant2);

		restaurant1.setRandomId();
		assertNotEquals(restaurant1, restaurant2);

		restaurant2.setRandomId();
		assertNotEquals(restaurant1, restaurant2);

		Dish dish = new Dish();
		assertNotEquals(restaurant1, dish);
	}
}
