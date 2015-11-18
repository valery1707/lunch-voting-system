package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.domain.TestEntity1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TestEntityRepo extends JpaRepository<TestEntity1, UUID>, JpaSpecificationExecutor<TestEntity1> {
}
