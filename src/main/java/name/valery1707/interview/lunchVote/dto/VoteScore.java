package name.valery1707.interview.lunchVote.dto;

import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;
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

		VoteScore that = (VoteScore) o;
		return new EqualsBuilder()
				.append(getRestaurantId(), that.getRestaurantId())
				.append(getRestaurantName(), that.getRestaurantName())
				.append(getCount(), that.getCount())
				.build();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(67, 91)
				.append(getRestaurantId())
				.append(getRestaurantName())
				.append(getCount())
				.hashCode();
	}
}
