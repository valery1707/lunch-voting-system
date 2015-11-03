package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.common.RestResult;
import name.valery1707.interview.lunchVote.domain.*;
import name.valery1707.interview.lunchVote.dto.VoteScore;
import name.valery1707.interview.lunchVote.dto.VoteStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromRequest;

@RestController
@RequestMapping("/api/restaurant")
@PreAuthorize("hasRole('ROLE_USER')")
public class RestaurantController {

	@Inject
	private RestaurantRepo repo;

	@Inject
	private PagingAndSortingRepository<Restaurant, UUID> baseRepo;

	@Inject
	private Validator validator;

	protected BindingResult validate(Object target, String objectName) {
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, objectName);
		validator.validate(target, errors);
		return errors;
	}

	protected <R> ResponseEntity<RestResult<R>> created(URI location, R body) {
		return ResponseEntity
				.created(location)
				.body(new RestResult<>(body));
	}

	protected <R extends IBaseEntity> ResponseEntity<RestResult<R>> created(R body, HttpServletRequest request, String idSegment) {
		return created(
				fromRequest(request)
						.pathSegment(idSegment)
						.build()
						.expand(body.getId())
						.toUri()
				, body);
	}

	protected <R> ResponseEntity<RestResult<R>> updated(R body) {
		return ResponseEntity.ok(new RestResult<>(body));
	}

	protected <R> ResponseEntity<RestResult<R>> invalid(BindingResult validate) {
		RestResult<R> result = new RestResult<>(false, null);
		validate.getFieldErrors().forEach(fieldError -> result.addError(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getArguments()));
		validate.getGlobalErrors().forEach(objectError -> result.addError("", objectError.getDefaultMessage(), objectError.getArguments()));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
	}

	protected <R> ResponseEntity<R> notFound() {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(null);
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public List<Restaurant> findAll() {
		return repo.findAll();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "", method = {RequestMethod.PUT, RequestMethod.POST})
	public ResponseEntity<RestResult<Restaurant>> create(@RequestBody Restaurant restaurant, HttpServletRequest request) {
		BindingResult validate = validate(restaurant, "restaurant");
		if (validate.hasErrors()) {
			return invalid(validate);
		}

		//Protect id and links from incorrect user input
		restaurant.setId(null);
		restaurant.getDishes().forEach(dish -> {
			dish.setId(null);
			dish.setRestaurant(restaurant);
		});
		//Save into database
		Restaurant saved = repo.saveAndFlush(restaurant);
		//Response
		return created(saved, request, "/{id}");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Restaurant> findById(@PathVariable UUID id) {
		Restaurant entity = repo.findOne(id);
		if (entity == null) {
			return notFound();
		}
		return ResponseEntity.ok(entity);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Restaurant> deleteById(@PathVariable UUID id) {
		Restaurant entity = repo.findOne(id);
		if (entity == null) {
			return notFound();
		}
		repo.delete(entity);
		return ResponseEntity.ok(entity);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<RestResult<Restaurant>> patchById(@PathVariable("id") UUID id, @RequestBody Restaurant patch) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return notFound();
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
	public ResponseEntity<RestResult<Restaurant>> updateById(@PathVariable("id") UUID id, @RequestBody Restaurant update) {
		Restaurant saved = repo.findOne(id);
		if (saved == null) {
			return notFound();
		}

		BindingResult validate = validate(update, "restaurant");
		if (validate.hasErrors()) {
			return invalid(validate);
		}

		update.setId(saved.getId());
		update.getDishes().removeIf(dish -> dish.getId() != null && !saved.getDishes().contains(dish));
		update.getDishes().forEach(dish -> dish.setRestaurant(saved));
		update = repo.saveAndFlush(update);
		return updated(update);
	}

	@Inject
	private VoteRepo voteRepo;

	@RequestMapping(value = "/vote", method = RequestMethod.GET)
	public List<VoteScore> voteScore(@RequestParam @DateTimeFormat(iso = DATE) Optional<LocalDate> date) {
		return voteRepo
				.findByDate(date.orElseGet(LocalDate::now))
				.stream()
				.collect(groupingBy(Vote::getRestaurant))
				.entrySet().stream()
				.map(entry -> new VoteScore(entry.getKey(), entry.getValue().size()))
				.sorted(VoteScore.COMPARATOR)
				.collect(toList());
	}

	@Value("${restaurant.vote.allowPassDate}")
	private boolean voteAllowPassDate;

	@Value("${restaurant.vote.maxVoteChangeHour}")
	private int maxVoteChangeHour;

	@Inject
	private AccountRepo accountRepo;

	@RequestMapping(value = "/{id}/vote", method = RequestMethod.POST)
	public ResponseEntity<VoteStatus> vote(@PathVariable("id") UUID restaurantId, @RequestParam @DateTimeFormat(iso = DATE_TIME) Optional<ZonedDateTime> datetime, Principal principal) {
		Restaurant restaurant = repo.findOne(restaurantId);
		if (restaurant == null) {
			return notFound();
		}

		ZonedDateTime now = datetime.filter(o -> voteAllowPassDate).orElseGet(ZonedDateTime::now).withZoneSameInstant(ZoneId.systemDefault());
		Account account = accountRepo.getByLogin(principal.getName());
		List<Vote> exists = voteRepo.findByDateAndAccount(now.toLocalDate(), account);
		Vote vote = exists.isEmpty() ? new Vote() : exists.get(0);

		//Can change mind?
		if (vote.getId() != null && now.getHour() >= maxVoteChangeHour) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(new VoteStatus(vote));
		}

		vote.setDateTime(now);
		vote.setAccount(account);
		vote.setRestaurant(restaurant);

		//Create or update
		ResponseEntity.BodyBuilder response;
		if (vote.getId() == null) {
			vote.setRandomId();
			response = ResponseEntity.status(HttpStatus.CREATED);
		} else {
			response = ResponseEntity.status(HttpStatus.ACCEPTED);
		}

		vote = voteRepo.saveAndFlush(vote);
		return response.body(new VoteStatus(vote));
	}
}
