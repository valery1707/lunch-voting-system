package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromRequest;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

	@Inject
	private RestaurantRepo repo;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public List<Restaurant> findAll() {
		return repo.findAll();
	}

	@RequestMapping(value = "", method = {RequestMethod.PUT, RequestMethod.POST})
	public ResponseEntity<Restaurant> create(@RequestBody Restaurant restaurant, HttpServletRequest request) {
		//Protect id and links from incorrect user input
		restaurant.setRandomId();
		restaurant.getDishes().forEach(dish -> {
			dish.setRandomId();
			dish.setRestaurant(restaurant);
		});
		//Save into database
		Restaurant saved = repo.saveAndFlush(restaurant);
		//Response
		return ResponseEntity
				.created(fromRequest(request)
						.pathSegment("{id}")
						.build()
						.expand(saved.getId())
						.toUri())
				.body(saved);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Restaurant> findById(@PathVariable UUID id) {
		Restaurant entity = repo.findOne(id);
		if (entity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(entity);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Restaurant> deleteById(@PathVariable UUID id) {
		Restaurant entity = repo.findOne(id);
		if (entity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(entity);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<Restaurant> patchById(@PathVariable("id") UUID id, @RequestBody Restaurant patch) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		//todo Copy all untouched fields from saved to patch
		return updateById(id, patch);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Restaurant> updateById(@PathVariable("id") UUID id, @RequestBody Restaurant update) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		//todo Validate entity
		update = repo.saveAndFlush(update);
		return ResponseEntity.ok(update);
	}
}
