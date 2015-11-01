package name.valery1707.interview.lunchVote.dto;

import name.valery1707.interview.lunchVote.domain.Restaurant;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class VoteScore {
	public static final Comparator<VoteScore> COMPARATOR = Comparator
			.comparing(VoteScore::getCount).reversed()
			.thenComparing(VoteScore::getRestaurantName);

	private final UUID restaurantId;
	private final String restaurantName;
	private final int count;

	public VoteScore(UUID restaurantId, String restaurantName, int count) {
		this.restaurantId = restaurantId;
		this.restaurantName = restaurantName;
		this.count = count;
	}

	public VoteScore(Restaurant restaurant, int count) {
		this(restaurant.getId(), restaurant.getName(), count);
	}

	public UUID getRestaurantId() {
		return restaurantId;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public int getCount() {
		return count;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VoteScore voteScore = (VoteScore) o;
		return Objects.equals(getRestaurantId(), voteScore.getRestaurantId()) &&
			   Objects.equals(getRestaurantName(), voteScore.getRestaurantName()) &&
			   Objects.equals(getCount(), voteScore.getCount());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRestaurantId(), getRestaurantName(), getCount());
	}
}
