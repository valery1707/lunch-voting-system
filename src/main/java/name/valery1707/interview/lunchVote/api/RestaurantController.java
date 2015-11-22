package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.common.BaseEntityController;
import name.valery1707.interview.lunchVote.common.RestFilter;
import name.valery1707.interview.lunchVote.common.RestResult;
import name.valery1707.interview.lunchVote.domain.Account;
import name.valery1707.interview.lunchVote.domain.Restaurant;
import name.valery1707.interview.lunchVote.domain.Vote;
import name.valery1707.interview.lunchVote.dto.VoteScore;
import name.valery1707.interview.lunchVote.dto.VoteStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@RequestMapping("/api/restaurant")
@PreAuthorize("hasRole('ROLE_USER')")
public class RestaurantController extends BaseEntityController<Restaurant, RestaurantRepo> {

	@Inject
	private RestaurantRepo repo;

	@Override
	@PreAuthorize("hasRole('ROLE_USER')")
	public Page<Restaurant> findAll(
			@PageableDefault(size = 20) @SortDefault("name") Pageable pageable,
			@RequestParam(name = "filter", required = false) List<String> filters,
			@RequestBody(required = false) RestFilter restFilter
	) {
		return super.findAll(pageable, filters, restFilter);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Restaurant> findById(@PathVariable UUID id) {
		return super.findById(id);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Restaurant>> create(@RequestBody Restaurant source, HttpServletRequest request) {
		return super.create(source, request);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Restaurant> deleteById(@PathVariable UUID id) {
		return super.deleteById(id);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Restaurant>> patchById(@PathVariable UUID id, @RequestBody Restaurant patch) {
		return super.patchById(id, patch);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Restaurant>> updateById(@PathVariable UUID id, @RequestBody Restaurant update) {
		return super.updateById(id, update);
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
