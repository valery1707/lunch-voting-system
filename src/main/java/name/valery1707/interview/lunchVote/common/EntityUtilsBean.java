package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.hibernate.internal.util.collections.IdentitySet;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.ClassUtils.isAssignable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Component
@Scope(scopeName = SCOPE_SINGLETON)
public class EntityUtilsBean {
	@Inject
	private EntityManager entityManager;

	@Inject
	private ConversionService conversionService;

	@SuppressWarnings("unchecked")
	public void deepClearId(IBaseEntity entity) {
		deepClearId(entity, new IdentitySet());
	}

	protected void deepClearId(IBaseEntity entity, Set<IBaseEntity> visited) {
		if (entity == null || !visited.add(entity)) {
			return;
		}
		entity.setId(null);
		getPluralAttributes(entity.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nested = attr.read(entity);
					for (IBaseEntity item : nested) {
						deepClearId(item, visited);
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepFixBackReference(IBaseEntity entity) {
		deepFixBackReference(entity, new IdentitySet());
	}

	protected void deepFixBackReference(IBaseEntity entity, Set<IBaseEntity> visited) {
		if (entity == null || !visited.add(entity)) {
			return;
		}
		getPluralAttributes(entity.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nested = attr.read(entity);
					for (IBaseEntity item : nested) {
						attr.writeBackReference(item, entity);
						deepFixBackReference(item, visited);
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepCleanNested(IBaseEntity src, IBaseEntity dst) {
		deepCleanNested(src, dst, new IdentitySet());
	}

	private void deepCleanNested(IBaseEntity src, IBaseEntity dst, Set<IBaseEntity> visited) {
		if (src == null || !visited.add(src)) {
			return;
		}
		getPluralAttributes(src.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nestedSrc = attr.read(src);
					Map<UUID, IBaseEntity> nestedSrcMap = nestedSrc.stream().collect(toMap(IBaseEntity::getId, i -> i));
					Collection<IBaseEntity> nestedDst = attr.read(dst);
					nestedDst.removeIf(itemDst -> itemDst.getId() != null && !nestedSrcMap.containsKey(itemDst.getId()));
					for (IBaseEntity itemDst : nestedDst) {
						IBaseEntity itemSrc = nestedSrcMap.get(itemDst.getId());
						if (itemSrc != null) {
							deepCleanNested(itemDst, itemSrc, visited);
						}
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepPatch(IBaseEntity src, IBaseEntity dst) {
		deepPatch(src, dst, new IdentitySet());
	}

	private void deepPatch(IBaseEntity src, IBaseEntity dst, Set<IBaseEntity> visited) {
		if (src == null || !visited.add(src)) {
			return;
		}
		patch(src, dst);
		getPluralAttributes(src.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nestedSrc = attr.read(src);
					Map<UUID, IBaseEntity> nestedSrcMap = nestedSrc.stream().collect(toMap(IBaseEntity::getId, i -> i));
					Collection<IBaseEntity> nestedDst = attr.read(dst);
					//Patch all items that exists in dst and in src
					nestedDst.stream()
							.filter(itemDst -> nestedSrcMap.containsKey(itemDst.getId()))
							.forEach(itemDst -> {
								deepPatch(nestedSrcMap.get(itemDst.getId()), itemDst, visited);
							});
					//Add collection items that exists in src but not exists in dst
					nestedDst.addAll(nestedSrc.stream().filter(itemSrc -> !nestedDst.contains(itemSrc)).collect(toList()));
				});
	}

	private void patch(IBaseEntity src, IBaseEntity dst) {
		//Patch singular attributes
		getSingularAttributes(src.getClass()).stream()
				.filter(attr -> isEmpty(attr.read(dst)))
				.forEach(attr -> {
					attr.write(dst, attr.read(src));
				});
	}

	private boolean isEmpty(Object value) {
		if (value instanceof String) {
			return isBlank(value.toString());
		} else {
			return value == null;
		}
	}

	private final Map<Class<? extends IBaseEntity>, List<SingularAttributeDescriptor>> singularAttributesCache = new ConcurrentHashMap<>();

	private List<SingularAttributeDescriptor> getSingularAttributes(Class<? extends IBaseEntity> clazz) {
		return singularAttributesCache.computeIfAbsent(clazz, this::findSingularAttributes);
	}

	@SuppressWarnings("unchecked")
	private List<SingularAttributeDescriptor> findSingularAttributes(Class<? extends IBaseEntity> clazz) {
		ManagedType<? extends IBaseEntity> type = entityManager.getEntityManagerFactory().getMetamodel().managedType(clazz);
		return type.getSingularAttributes().stream()
				.filter(attr -> !attr.isId())
				.map(attr -> {
					PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, attr.getName());
					return new SingularAttributeDescriptor(descriptor);
				})
				.collect(toList())
				;
	}

	private static class SingularAttributeDescriptor {
		private final PropertyDescriptor propertyDescriptor;

		private SingularAttributeDescriptor(PropertyDescriptor propertyDescriptor) {
			this.propertyDescriptor = propertyDescriptor;
		}

		public Object read(IBaseEntity entity) {
			try {
				return propertyDescriptor.getReadMethod().invoke(entity);
			} catch (IllegalAccessException | InvocationTargetException ignored) {
				return null;
			}
		}

		public void write(IBaseEntity entity, Object value) {
			try {
				propertyDescriptor.getWriteMethod().invoke(entity, value);
			} catch (IllegalAccessException | InvocationTargetException ignored) {
			}
		}
	}

	private final Map<Class<? extends IBaseEntity>, List<PluralAttributeDescriptor>> pluralAttributesCache = new ConcurrentHashMap<>();

	private List<PluralAttributeDescriptor> getPluralAttributes(Class<? extends IBaseEntity> clazz) {
		return pluralAttributesCache.computeIfAbsent(clazz, this::findPluralAttributes);
	}

	@SuppressWarnings("unchecked")
	private List<PluralAttributeDescriptor> findPluralAttributes(Class<? extends IBaseEntity> clazz) {
		ManagedType<? extends IBaseEntity> type = entityManager.getEntityManagerFactory().getMetamodel().managedType(clazz);
		return type.getPluralAttributes().stream()
				.filter(attr -> IBaseEntity.class.isAssignableFrom(attr.getElementType().getJavaType()))
				.map(attr -> (PluralAttribute<IBaseEntity, Collection, IBaseEntity>) attr)
				.map(attr -> {
					PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, attr.getName());
					Field field = findField(clazz, attr.getName());
					return new PluralAttributeDescriptor(attr.getElementType().getJavaType(), descriptor, field);
				})
				.collect(toList())
				;
	}

	@Nullable
	private Field findField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getField(fieldName);
		} catch (NoSuchFieldException ignored) {
		}
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ignored) {
		}
		return null;
	}

	private static class PluralAttributeDescriptor {
		private final Class<? extends IBaseEntity> targetClass;
		private final PropertyDescriptor propertyDescriptor;
		@Nullable
		private final Field field;
		private final boolean isTransient;
		@Nullable
		private final PropertyDescriptor backReferenceDescriptor;

		private PluralAttributeDescriptor(Class<? extends IBaseEntity> targetClass, PropertyDescriptor propertyDescriptor, @Nullable Field field) {
			this.targetClass = targetClass;
			this.propertyDescriptor = propertyDescriptor;
			this.field = field;

			Transient transientA = field != null
					? field.getAnnotation(Transient.class)
					: propertyDescriptor.getReadMethod().getAnnotation(Transient.class);
			isTransient = transientA != null;

			PropertyDescriptor backReferenceDescriptor = null;
			if (field != null) {
				if (!isTransient) {
					OneToMany oneToMany = field.getAnnotation(OneToMany.class);
					if (oneToMany != null) {
						backReferenceDescriptor = BeanUtils.getPropertyDescriptor(targetClass, oneToMany.mappedBy());
					}
				}
			}
			this.backReferenceDescriptor = backReferenceDescriptor;
		}

		@SuppressWarnings("unchecked")
		public Collection<IBaseEntity> read(IBaseEntity container) {
			try {
				return (Collection<IBaseEntity>) propertyDescriptor.getReadMethod().invoke(container);
			} catch (IllegalAccessException | InvocationTargetException ignored) {
				return Collections.emptyList();
			}
		}

		public void writeBackReference(IBaseEntity entity, IBaseEntity backReference) {
			if (backReferenceDescriptor != null) {
				try {
					backReferenceDescriptor.getWriteMethod().invoke(entity, backReference);
				} catch (IllegalAccessException | InvocationTargetException ignored) {
				}
			}
		}
	}

	public <T extends IBaseEntity> Specification<T> complexFilter(Class<T> entityClass, RestFilter filter) {
		switch (filter.getType()) {
			case AND:
				return filter.getAnd().stream()
						.map(f -> complexFilter(entityClass, f))
						.map(Specifications::where)
						.reduce(Specifications.where(null), Specifications::and);
			case OR:
				return filter.getOr().stream()
						.map(f -> complexFilter(entityClass, f))
						.map(Specifications::where)
						.reduce(Specifications.where(null), Specifications::or);
			case NOT:
				return Specifications.not(complexFilter(entityClass, filter.getNot()));
			case FILTER:
			default:
				FILTER_OPERATION operation;
				try {
					operation = FILTER_OPERATION.byCode(filter.getOperation());
				} catch (IllegalArgumentException ex) {
					throw unknownFilterOperation(filter.getField(), filter.getOperation(), filter.getValue());
				}
				return simpleFilter(entityClass, filter.getField(), operation, filter.getValue());
		}
	}

	public <T extends IBaseEntity> Specification<T> simpleFilter(Class<T> entityClass, List<String> filters) {
//		noNullElements(filters, "Filters must not contain null element");
		Specifications<T> spec = null;
		for (String filter : filters) {
			Specification<T> filterSpec = simpleFilter(entityClass, filter);
			spec = spec == null ? Specifications.where(filterSpec) : spec.and(filterSpec);
		}
		return spec;
	}

	private static final Pattern SIMPLE_FILTER_PATTERN = Pattern.compile(
			"^" +
			"([\\w\\.]+);" +                 //Field name
			"(" + Stream.of(FILTER_OPERATION.values()).map(FILTER_OPERATION::getCode).map(Pattern::quote).collect(joining("|")) + ");" +  //Operation
			//todo Between
			"(.+)?" +                               //Value
			"$");

	public enum FILTER_OPERATION {
		LESS("<"),
		LESS_OR_EQUAL("<="),
		EQUAL("="),
		GREATER_OR_EQUAL("=>"),
		GREATER(">"),
		NOT_EQUAL("!="),
		LIKE("~"),
		NOT_LIKE("!~"),
		CASE_SENSITIVE_LIKE("~!"),
		CASE_SENSITIVE_NOT_LIKE("!~!"),
		IS_NULL("_"),
		NOT_NULL("!_"),
		UNKNOWN("?");

		private final String code;

		FILTER_OPERATION(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

		@Nonnull
		public static FILTER_OPERATION byCode(String code) {
			for (FILTER_OPERATION operation : values()) {
				if (operation.getCode().equals(code)) {
					return operation;
				}
			}
			throw new IllegalArgumentException("Unknown operation code: " + code);
		}
	}

	public <T extends IBaseEntity, V extends Comparable<V>> Specification<T> simpleFilter(Class<T> entityClass, String filter) {
		Matcher matcher = SIMPLE_FILTER_PATTERN.matcher(filter);
		Assert.state(matcher.matches(), "Incorrect filter format: " + filter);
		String fieldPath = matcher.group(1);
		FILTER_OPERATION operation = FILTER_OPERATION.byCode(matcher.group(2));
		String valueRaw = operation.getCode().contains("_") ? null : matcher.group(3);
		return simpleFilter(entityClass, fieldPath, operation, valueRaw);
	}

	public <T extends IBaseEntity, V extends Comparable<V>> Specification<T> simpleFilter(Class<T> entityClass, String fieldPath, FILTER_OPERATION operation, Object valueRaw) {
		String[] joinPath = fieldPath.split("\\.");
		String fieldName = joinPath[joinPath.length - 1];
		String[] joinPathFinal = Arrays.copyOf(joinPath, joinPath.length - 1);

		Attribute<? super T, ?> attribute = findAttribute(entityClass, fieldPath);
		if (isAssignable(attribute.getJavaType(), Number.class) || isAssignable(attribute.getJavaType(), Boolean.class)) {
			Assert.state(!operation.getCode().contains("~"), format("Incorrect filter operation: field [%s] with type '%s' could not be filtered by operation [%s]", fieldPath, attribute.getJavaType().getCanonicalName(), operation));
		}
		V value = convertToTargetType(attribute.getJavaType(), valueRaw);

		return (root, query, cb) -> {
			From<T, ?> join = root;
			for (String path : joinPathFinal) {
				join = findOrCreateJoin(join, path);
			}
			Path<V> field = join.get(fieldName);
			Path<String> fieldString = join.get(fieldName);
			switch (operation) {
				case LESS:
					return cb.lessThan(field, value);
				case LESS_OR_EQUAL:
					return cb.lessThanOrEqualTo(field, value);
				case EQUAL:
					return cb.equal(field, value);
				case GREATER_OR_EQUAL:
					return cb.greaterThanOrEqualTo(field, value);
				case GREATER:
					return cb.greaterThan(field, value);
				case NOT_EQUAL:
					return cb.notEqual(field, value);
				case LIKE:
					return cb.like(cb.lower(fieldString), toLikePattern(valueRaw).toLowerCase(), '\\');
				case NOT_LIKE:
					return cb.notLike(cb.lower(fieldString), toLikePattern(valueRaw).toLowerCase(), '\\');
				case CASE_SENSITIVE_LIKE:
					return cb.like(fieldString, toLikePattern(valueRaw), '\\');
				case CASE_SENSITIVE_NOT_LIKE:
					return cb.notLike(fieldString, toLikePattern(valueRaw), '\\');
				case IS_NULL:
					return cb.isNull(field);
				case NOT_NULL:
					return cb.isNotNull(field);
				default:
					throw unknownFilterOperation(fieldPath, operation.getCode(), valueRaw);
			}
		};
	}

	@NotNull
	private IllegalStateException unknownFilterOperation(String fieldPath, String operation, Object valueRaw) {
		return new IllegalStateException(format("Incorrect filter operation: unknown operation '%s' in filter: %s %s %s", operation, fieldPath, operation, valueRaw));
	}

	@SuppressWarnings("unchecked")
	private <V extends Comparable<V>> V convertToTargetType(Class<?> javaType, Object raw) {
		if (raw == null) {
			return null;
		}
		if (conversionService.canConvert(raw.getClass(), javaType)) {
			try {
				Object convert = conversionService.convert(raw, javaType);
				if (convert instanceof Comparable) {
					return (V) convert;
				}
			} catch (ConversionFailedException ignored) {
			}
		}
		throw new IllegalStateException(format("Incorrect filter value: could not convert value '%s' with type '%s' into '%s' type", raw, raw.getClass(), javaType));
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	private <T extends IBaseEntity> From<T, ?> findOrCreateJoin(@Nonnull From<T, ?> source, String path) {
		Optional<? extends Join<?, ?>> existsJoin = source.getJoins().stream().filter(j -> j.getAttribute().getName().equals(path)).findAny();
		if (existsJoin.isPresent()) {
			return (From<T, ?>) existsJoin.get();
		} else {
			return source.join(path, JoinType.LEFT);
		}
	}

	/**
	 * Deep search for field (every nested) and return attribute. If field dos not exists - throw IllegalStateException
	 *
	 * @param entityClass Entity class
	 * @param fieldPath   Field name
	 * @param <T>         Entity type
	 * @return Attribute for given name
	 * @throws IllegalStateException If field does not found in entity
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	private <T extends IBaseEntity> Attribute<? super T, ?> findAttribute(Class<T> entityClass, String fieldPath) {
		ManagedType<T> managedType = entityManager.getEntityManagerFactory().getMetamodel().managedType(entityClass);

		String field, rest;
		if (fieldPath.contains(".")) {
			int i = fieldPath.indexOf('.');
			field = fieldPath.substring(0, i);
			rest = fieldPath.substring(i + 1);
		} else {
			field = fieldPath;
			rest = null;
		}

		try {
			Attribute<? super T, ?> attribute = managedType.getAttribute(field);
			if (rest == null) {
				return attribute;
			} else if (attribute instanceof PluralAttribute<?, ?, ?>) {
				return findAttribute(((PluralAttribute) attribute).getElementType().getJavaType(), rest);
			} else {
				Class javaType = ((SingularAttribute) attribute).getJavaType();
				if (isAssignable(javaType, IBaseEntity.class)) {
					return findAttribute(javaType, rest);
				} else {
					throw new IllegalStateException(format("Unknown field [%s] within class [%s]", rest, javaType));
				}
			}
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException(format("Unknown field [%s] within class [%s]", field, entityClass.getCanonicalName()));
		}
	}

	@Nonnull
	static String toLikePattern(@Nullable Object rawPattern) {
		if (rawPattern == null) {
			return "%";
		}
		String simplePattern = rawPattern.toString();
		if (isBlank(simplePattern)) {
			return "%";
		}
		boolean isFullPattern = simplePattern.contains("*") || simplePattern.contains("?");
		String pattern = simplePattern
				.replaceAll("%", "\\\\%")//escape
				.replaceAll("\\*", "%")//replace
				.replaceAll("_", "\\\\_")//escape
				.replaceAll("\\?", "_")//replace
				;
		if (isFullPattern) {
			return pattern;
		} else {
			return "%" + pattern + "%";
		}
	}
}
