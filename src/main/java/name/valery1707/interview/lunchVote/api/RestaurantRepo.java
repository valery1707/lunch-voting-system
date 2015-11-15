package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RestaurantRepo extends JpaRepository<Restaurant, UUID>, JpaSpecificationExecutor<Restaurant> {
}
