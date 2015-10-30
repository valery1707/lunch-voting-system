package name.valery1707.interview.lunchVote.domain;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IBaseEntity {
	UUID getId();

	void setId(UUID is);

	@Nonnull
	default UUID setRandomId() {
		setId(UUID.randomUUID());
		return getId();
	}
}
