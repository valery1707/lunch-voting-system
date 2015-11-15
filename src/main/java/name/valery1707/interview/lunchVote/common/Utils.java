package name.valery1707.interview.lunchVote.common;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Utils {
	Utils() throws IllegalAccessException {
		throw new IllegalAccessException();
	}

	public static <T> List<T> iterableToList(Iterable<T> iterable) {
		if (iterable instanceof List) {
			return (List<T>) iterable;
		} else if (iterable instanceof Collection) {
			return new ArrayList<>((Collection<T>) iterable);
		} else {
			ArrayList<T> list = new ArrayList<>();
			for (T bean : iterable) {
				list.add(bean);
			}
			return list;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getGenericType(Class<?> implementationClazz, Class<?> interfaceClass, String typeId) {
		int typeIndex = -1;
		TypeVariable<? extends Class<?>>[] typeParameters = interfaceClass.getTypeParameters();
		for (int i = 0; i < typeParameters.length && typeIndex < 0; i++) {
			TypeVariable<? extends Class<?>> typeVariable = typeParameters[i];
			if (typeVariable.getName().equals(typeId)) {
				typeIndex = i;
			}
		}
		Assert.state(typeIndex >= 0);//todo Message

		ResolvableType type = ResolvableType.forClass(interfaceClass, implementationClazz);
		return (Class<T>) type.getGeneric(typeIndex).getRawClass();
	}
}
