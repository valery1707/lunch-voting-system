package name.valery1707.interview.lunchVote.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@SuppressWarnings("unused")
public class Account extends ABaseEntity {
	@Column
	private String login;

	@Column
	private String password;

	@Column
	private String firstName;

	@Column
	private String lastName;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
