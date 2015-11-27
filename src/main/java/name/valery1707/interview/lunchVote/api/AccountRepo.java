package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
	List<Account> findByLogin(String login);

	Account getByLogin(String login);
}
