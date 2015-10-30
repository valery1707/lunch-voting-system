package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {
}
