package name.valery1707.interview.lunchVote.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ABaseEntity implements IBaseEntity {
	@Id
	@GeneratedValue
	@Column
	private UUID id;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}

		ABaseEntity that = (ABaseEntity) o;
		return getId() != null && new EqualsBuilder().append(getId(), that.getId()).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(67, 91).append(id).hashCode();
	}
}
