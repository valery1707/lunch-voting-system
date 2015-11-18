package name.valery1707.interview.lunchVote.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@SuppressWarnings("unused")
public class TestEntity1 extends ABaseEntity {
	@Column
	@NotNull
	@Size(min = 1, max = 255)
	private String name;

	@ManyToOne
	private TestEntity2 secondLink;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TestEntity2> secondCollection;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestEntity2 getSecondLink() {
		return secondLink;
	}

	public void setSecondLink(TestEntity2 secondLink) {
		this.secondLink = secondLink;
	}

	public Set<TestEntity2> getSecondCollection() {
		return secondCollection;
	}

	public void setSecondCollection(Set<TestEntity2> secondCollection) {
		this.secondCollection = secondCollection;
	}
}
