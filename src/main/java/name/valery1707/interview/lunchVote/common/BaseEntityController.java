package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static name.valery1707.interview.lunchVote.common.SimpleDistinctSpec.distinct;
import static name.valery1707.interview.lunchVote.common.Utils.getGenericType;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromRequest;

public abstract class BaseEntityController<T extends IBaseEntity, REPO extends PagingAndSortingRepository<T, UUID> & JpaSpecificationExecutor<T>> {

	private final Class<T> entityClass;

	@Inject
	private REPO repository;

	@Inject
	private Validator validator;

	@Inject
	private EntityUtilsBean entityUtils;

	public BaseEntityController() {
		entityClass = getGenericType(getClass(), BaseEntityController.class, "T");
	}

	protected Class<T> entityClass() {
		return entityClass;
	}

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
	public Page<T> findAll(
			Pageable pageable,
			@RequestParam(name = "filter", required = false) List<String> filters,
			@RequestBody(required = false) RestFilter restFilter
	) {
		Specification<T> spec = distinct();
		if (restFilter != null) {
			BindingResult validate = validate(restFilter, "filter");
			if (validate.hasErrors()) {
				throw new IllegalStateException();//todo Describe exception
			}
			Specification<T> filter = entityUtils.complexFilter(entityClass(), restFilter);
			spec = Specifications.where(spec).and(filter);
		}
		if (filters != null) {
			Specification<T> filter = entityUtils.simpleFilter(entityClass(), filters);
			spec = Specifications.where(spec).and(filter);
		}
		return repository.findAll(spec, pageable);
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

	@ExceptionHandler(IllegalStateException.class)
	@ResponseBody
	public ResponseEntity<RestError> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(RestError.fromException(ex, request.getServletPath()));
	}
}
