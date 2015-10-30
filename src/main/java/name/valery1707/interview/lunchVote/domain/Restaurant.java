package name.valery1707.interview.lunchVote.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@SuppressWarnings("unused")
public class Restaurant extends ABaseEntity {
	@Column
	private String name;

	@OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
	private Set<Dish> dishes = new HashSet<>(0);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Dish> getDishes() {
		return dishes;
	}

	public void setDishes(Set<Dish> dishes) {
		this.dishes = dishes;
	}
}
