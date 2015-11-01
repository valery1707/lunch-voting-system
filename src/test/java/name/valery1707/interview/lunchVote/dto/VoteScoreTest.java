package name.valery1707.interview.lunchVote.dto;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class VoteScoreTest {
	@Test
	public void testComparator() throws Exception {
		List<VoteScore> scoreSorted = Arrays.asList(score("aa", 2), score("ab", 2), score("aa", 1), score("ab", 1));
		List<VoteScore> scoreUnsorted = new ArrayList<>(scoreSorted);
		Collections.shuffle(scoreUnsorted);
		assertThat(sort(scoreUnsorted))
				.hasSameSizeAs(scoreSorted)
				.containsOnlyElementsOf(scoreUnsorted)
				.containsExactlyElementsOf(scoreSorted);
	}

	private static Set<VoteScore> sort(Collection<VoteScore> src) {
		TreeSet<VoteScore> set = new TreeSet<>(VoteScore.COMPARATOR);
		set.addAll(src);
		return set;
	}

	private static VoteScore score(String name, int count) {
		return new VoteScore(null, name, count);
	}
}
