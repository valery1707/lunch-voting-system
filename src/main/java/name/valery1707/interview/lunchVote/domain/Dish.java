package name.valery1707.interview.lunchVote.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@SuppressWarnings("unused")
public class Dish extends ABaseEntity {
	@ManyToOne
	private Restaurant restaurant;

	@Column
	@NotNull
	private String name;

	@Column
	@NotNull
	@Min(0)
	private Double price;

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
