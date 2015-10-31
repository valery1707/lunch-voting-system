package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Dish;
import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import name.valery1707.interview.lunchVote.domain.Restaurant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromRequest;

@RestController
@RequestMapping("/api/restaurant")
@PreAuthorize("hasRole('ROLE_USER')")
public class RestaurantController {

	@Inject
	private RestaurantRepo repo;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public List<Restaurant> findAll() {
		return repo.findAll();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "", method = {RequestMethod.PUT, RequestMethod.POST})
	public ResponseEntity<Restaurant> create(@RequestBody Restaurant restaurant, HttpServletRequest request) {
		//Protect id and links from incorrect user input
		restaurant.setId(null);
		restaurant.getDishes().forEach(dish -> {
			dish.setId(null);
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

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Restaurant> deleteById(@PathVariable UUID id) {
		Restaurant entity = repo.findOne(id);
		if (entity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		repo.delete(entity);
		return ResponseEntity.ok(entity);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<Restaurant> patchById(@PathVariable("id") UUID id, @RequestBody Restaurant patch) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		//Copy all untouched fields from saved to patch
		if (isBlank(patch.getName())) {
			patch.setName(saved.getName());
		}
		patch.setId(saved.getId());

		//Remove all not owr dishes
		Map<UUID, Dish> dishes = saved.getDishes().stream().collect(toMap(IBaseEntity::getId, Function.<Dish>identity()));

		//Copy all untouched fields from saved to patch
		patch.getDishes().stream()
				.filter(dish -> dish.getId() != null)
				.forEach(dishPatch -> {
					Dish dishSaved = dishes.get(dishPatch.getId());
					if (isBlank(dishPatch.getName())) {
						dishPatch.setName(dishSaved.getName());
					}
					if (dishPatch.getPrice() == null) {
						dishPatch.setPrice(dishSaved.getPrice());
					}
				});
		//Copy all untouched dishes
		patch.getDishes().addAll(saved.getDishes().stream().filter(dish -> !patch.getDishes().contains(dish)).collect(toSet()));

		return updateById(id, patch);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Restaurant> updateById(@PathVariable("id") UUID id, @RequestBody Restaurant update) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		update.setId(saved.getId());
		update.getDishes().removeIf(dish -> dish.getId() != null && !saved.getDishes().contains(dish));
		update.getDishes().forEach(dish -> dish.setRestaurant(saved));
		//todo Validate entity
		update = repo.saveAndFlush(update);
		return ResponseEntity.ok(update);
	}
}
