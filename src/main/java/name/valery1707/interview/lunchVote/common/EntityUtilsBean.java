package name.valery1707.interview.lunchVote.common;

import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.hibernate.internal.util.collections.IdentitySet;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Component
@Scope(scopeName = SCOPE_SINGLETON)
public class EntityUtilsBean {
	@Inject
	private EntityManager entityManager;

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
}
