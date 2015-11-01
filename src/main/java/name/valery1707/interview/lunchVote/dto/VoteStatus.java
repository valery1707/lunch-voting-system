package name.valery1707.interview.lunchVote.dto;

import name.valery1707.interview.lunchVote.domain.Vote;

import java.time.ZonedDateTime;
import java.util.UUID;

public class VoteStatus {
	private final UUID restaurantId;
	private final ZonedDateTime dateTime;

	private VoteStatus(UUID restaurantId, ZonedDateTime dateTime) {
		this.restaurantId = restaurantId;
		this.dateTime = dateTime;
	}

	public VoteStatus(Vote vote) {
		this(vote.getRestaurant().getId(), vote.getDateTime());
	}

	public UUID getRestaurantId() {
		return restaurantId;
	}

	public ZonedDateTime getDateTime() {
		return dateTime;
	}
}
