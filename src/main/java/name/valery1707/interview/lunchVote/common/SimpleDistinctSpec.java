package name.valery1707.interview.lunchVote.common;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class SimpleDistinctSpec<T> implements Specification<T> {
	private static final SimpleDistinctSpec<?> INSTANCE = new SimpleDistinctSpec<>();

	@SuppressWarnings("unchecked")
	public static <T> Specification<T> instance() {
		return (Specification<T>) INSTANCE;
	}

	public static <T> Specification<T> distinct() {
		return instance();
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		query.distinct(true);
		return null;
	}

	@Override
	public String toString() {
		return "distinct()";
	}
}
