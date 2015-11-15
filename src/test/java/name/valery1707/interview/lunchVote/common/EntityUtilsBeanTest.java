package name.valery1707.interview.lunchVote.common;

import org.junit.Test;

import static name.valery1707.interview.lunchVote.common.EntityUtilsBean.toLikePattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityUtilsBeanTest {

	@Test
	public void testToLikePattern() throws Exception {
		assertNotNull(toLikePattern(null));
		assertEquals("%", toLikePattern(null));
		assertEquals("%", toLikePattern(""));
		assertEquals("%abc%", toLikePattern("abc"));
		assertEquals("%bc", toLikePattern("*bc"));
		assertEquals("_bc", toLikePattern("?bc"));
		assertEquals("%abc\\%cde%", toLikePattern("abc%cde"));
		assertEquals("%abc\\_cde%", toLikePattern("abc_cde"));
	}
}
