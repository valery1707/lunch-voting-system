package name.valery1707.interview.lunchVote.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@SuppressWarnings("unused")
public class TestEntity2 extends ABaseEntity {
	@ManyToOne
	private TestEntity1 parent;

	@Column
	@NotNull
	@Size(min = 1, max = 255)
	private String name;

	@ManyToOne
	private TestEntity3 thirdLink;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<TestEntity3> thirdCollection;

	public TestEntity1 getParent() {
		return parent;
	}

	public void setParent(TestEntity1 parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestEntity3 getThirdLink() {
		return thirdLink;
	}

	public void setThirdLink(TestEntity3 thirdLink) {
		this.thirdLink = thirdLink;
	}

	public Set<TestEntity3> getThirdCollection() {
		return thirdCollection;
	}

	public void setThirdCollection(Set<TestEntity3> thirdCollection) {
		this.thirdCollection = thirdCollection;
	}
}
