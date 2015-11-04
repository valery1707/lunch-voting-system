package name.valery1707.interview.lunchVote.common;

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
}
