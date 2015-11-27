package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.common.BaseEntityController;
import name.valery1707.interview.lunchVote.common.RestFilter;
import name.valery1707.interview.lunchVote.common.RestResult;
import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AccountController extends BaseEntityController<Account, AccountRepo> {

	@Inject
	private AccountRepo repo;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<Account> findAll(
			Pageable pageable,
			@RequestParam(name = "filter", required = false) List<String> filters,
			@RequestBody(required = false) RestFilter restFilter
	) {
		return super.findAll(pageable, filters, restFilter);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Account>> create(@RequestBody Account source, HttpServletRequest request) {
		return super.create(source, request);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Account> findById(@PathVariable UUID id) {
		return super.findById(id);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Account> deleteById(@PathVariable UUID id) {
		return super.deleteById(id);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Account>> patchById(@PathVariable("id") UUID id, @RequestBody Account patch) {
		return super.patchById(id, patch);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<RestResult<Account>> updateById(@PathVariable("id") UUID id, @RequestBody Account update) {
		return super.updateById(id, update);
	}
}
