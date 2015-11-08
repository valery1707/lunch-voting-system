package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromRequest;

public abstract class BaseEntityController<T extends IBaseEntity> {

	@Inject
	private PagingAndSortingRepository<T, UUID> repository;

	@Inject
	private Validator validator;

	@Inject
	private EntityUtilsBean entityUtils;

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

	/**
	 * Clear id for all objects in this entity
	 *
	 * @param root Root entity
	 */
	protected void deepClearId(T root) {
		entityUtils.deepClearId(root);
	}

	/**
	 * Correctly set back reference links in nested collections
	 *
	 * @param root Root entity
	 */
	protected void deepFixBackReference(T root) {
		entityUtils.deepFixBackReference(root);
	}

	/**
	 * Copy values for all null fields in {@code dst} from corresponding field from {@code src}
	 *
	 * @param src Current entity values from database
	 * @param dst Entity with partially filled fields
	 */
	protected void deepPatch(T src, T dst) {
		entityUtils.deepPatch(src, dst);
	}

	/**
	 * Remove from {@code dst} nested collections entity with id and does not exists in save nested collection in {@code src}
	 *
	 * @param src Current entity values from database
	 * @param dst Entity prepared for save
	 */
	protected void deepCleanNested(T src, T dst) {
		entityUtils.deepCleanNested(src, dst);
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public Page<T> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@RequestMapping(value = "", method = {RequestMethod.PUT, RequestMethod.POST})
	public ResponseEntity<RestResult<T>> create(@RequestBody T source, HttpServletRequest request) {
		BindingResult validate = validate(source, "root");
		if (validate.hasErrors()) {
			return invalid(validate);
		}

		//Protect id and links from incorrect user input
		deepClearId(source);
		deepFixBackReference(source);

		T saved = repository.save(source);
		return created(saved, request, "/{id}");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<T> findById(@PathVariable UUID id) {
		T entity = repository.findOne(id);
		if (entity == null) {
			return notFound();
		}

		return ResponseEntity.ok(entity);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<T> deleteById(@PathVariable UUID id) {
		T entity = repository.findOne(id);
		if (entity == null) {
			return notFound();
		}

		repository.delete(entity);
		return ResponseEntity.ok(entity);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<RestResult<T>> patchById(@PathVariable("id") UUID id, @RequestBody T patch) {
		T saved = repository.findOne(id);
		if (saved == null) {
			return notFound();
		}

		//Copy all untouched fields from saved to patch
		patch.setId(saved.getId());
		deepPatch(saved, patch);

		return updateById(id, patch);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<RestResult<T>> updateById(@PathVariable("id") UUID id, @RequestBody T update) {
		T saved = repository.findOne(id);
		if (saved == null) {
			return notFound();
		}

		update.setId(saved.getId());
		deepCleanNested(saved, update);
		deepFixBackReference(update);

		BindingResult validate = validate(update, "root");
		if (validate.hasErrors()) {
			return invalid(validate);
		}

		update = repository.save(update);
		return updated(update);
	}
}
