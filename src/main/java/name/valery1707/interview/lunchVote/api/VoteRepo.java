package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.Account;
import name.valery1707.interview.lunchVote.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VoteRepo extends JpaRepository<Vote, UUID> {

	@Query("SELECT T FROM Vote AS T WHERE TRUNC(T.dateTime) = :date")
	List<Vote> findByDate(@Param("date") LocalDate date);

	@Query("SELECT T FROM Vote AS T WHERE TRUNC(T.dateTime) = :date AND T.account = :account")
	List<Vote> findByDateAndAccount(@Param("date") LocalDate date, @Param("account") Account account);
}
