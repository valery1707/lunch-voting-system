package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.hibernate.internal.util.collections.IdentitySet;
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
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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

	protected void deepClearId(IBaseEntity entity, Set<IBaseEntity> seen) {
		if (entity == null || !seen.add(entity)) {
			return;
		}
		entity.setId(null);
		getPluralAttributes(entity.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nested = attr.read(entity);
					for (IBaseEntity item : nested) {
						deepClearId(item, seen);
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepFixBackReference(IBaseEntity entity) {
		deepFixBackReference(entity, new IdentitySet());
	}

	protected void deepFixBackReference(IBaseEntity entity, Set<IBaseEntity> seen) {
		if (entity == null || !seen.add(entity)) {
			return;
		}
		getPluralAttributes(entity.getClass()).stream()
				.forEach(attr -> {
					Collection<IBaseEntity> nested = attr.read(entity);
					for (IBaseEntity item : nested) {
						attr.writeBackReference(item, entity);
						deepFixBackReference(item, seen);
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepCleanNested(IBaseEntity src, IBaseEntity dst) {
		deepCleanNested(src, dst, new IdentitySet());
	}

	private void deepCleanNested(IBaseEntity src, IBaseEntity dst, Set<IBaseEntity> seen) {
		if (src == null || !seen.add(src)) {
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
							deepCleanNested(itemDst, itemSrc, seen);
						}
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void deepPatch(IBaseEntity src, IBaseEntity dst) {
		deepPatch(src, dst, new IdentitySet());
	}

	private void deepPatch(IBaseEntity src, IBaseEntity dst, Set<IBaseEntity> seen) {
		if (src == null || !seen.add(src)) {
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
								deepPatch(nestedSrcMap.get(itemDst.getId()), itemDst, seen);
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
			"(<|<=|=|=>|>|!=|~|!~|~!|!~!);" +//Operation: LESS(<), LESS_OR_EQUAL(<=), EQUAL(=), GREATER_OR_EQUAL(=>), GREATER(>), NOT_EQUAL(!=), LIKE(~), NOT_LIKE(!~), CASE_SENSITIVE_LIKE(~!), CASE_SENSITIVE_NOT_LIKE(!~!)
			//todo Between
			"(.+)" +                         //Value
			"$");

	public <T extends IBaseEntity, V extends Comparable<V>> Specification<T> simpleFilter(Class<T> entityClass, String filter) {
		Matcher matcher = SIMPLE_FILTER_PATTERN.matcher(filter);
		Assert.state(matcher.matches(), "Incorrect filter format: " + filter);
		String fieldPath = matcher.group(1);
		String operation = matcher.group(2);
		String valueRaw = matcher.group(3);
		String[] joinPath = fieldPath.split("\\.");
		String fieldName = joinPath[joinPath.length - 1];
		String[] joinPathFinal = Arrays.copyOf(joinPath, joinPath.length - 1);

		Attribute<? super T, ?> attribute = findAttribute(entityClass, fieldPath);
		if (isAssignable(attribute.getJavaType(), Number.class) || isAssignable(attribute.getJavaType(), Boolean.class)) {
			Assert.state(!operation.contains("~"), format("Incorrect filter operation: field [%s] with type '%s' could not be filtered by operation [%s]", fieldPath, attribute.getJavaType(), operation));
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
				case "<":
					return cb.lessThan(field, value);
				case "<=":
					return cb.lessThanOrEqualTo(field, value);
				case "=":
					return cb.equal(field, value);
				case "=>":
					return cb.greaterThanOrEqualTo(field, value);
				case ">":
					return cb.greaterThan(field, value);
				case "!=":
					return cb.notEqual(field, value);
				case "~":
					return cb.like(cb.lower(fieldString), toLikePattern(valueRaw).toLowerCase(), '\\');
				case "!~":
					return cb.notLike(cb.lower(fieldString), toLikePattern(valueRaw).toLowerCase(), '\\');
				case "~!":
					return cb.like(fieldString, toLikePattern(valueRaw), '\\');
				case "!~!":
					return cb.notLike(fieldString, toLikePattern(valueRaw), '\\');
				default:
					throw new IllegalStateException(format("Unknown operation '%s' in filter: %s", operation, filter));
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <V extends Comparable<V>> V convertToTargetType(Class<?> javaType, String raw) {
		if (raw == null) {
			return null;
		}
		if (conversionService.canConvert(String.class, javaType)) {
			try {
				Object convert = conversionService.convert(raw, javaType);
				if (convert instanceof Comparable) {
					return (V) convert;
				}
			} catch (ConversionFailedException ignored) {
			}
		}
		throw new IllegalStateException(format("Incorrect filter value: could not convert string '%s' into '%s' type", raw, javaType));
	}

	@SuppressWarnings("unchecked")
	@Nonnull
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
				throw new IllegalStateException(format("Unknown field [%s] within class [%s]", rest, entityClass.getCanonicalName()));
			}
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException(format("Unknown field [%s] within class [%s]", field, entityClass.getCanonicalName()));
		}
	}

	@Nonnull
	static String toLikePattern(@Nullable String simplePattern) {
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
