package name.valery1707.interview.lunchVote.api;

import name.valery1707.interview.lunchVote.common.BaseEntityController;
import name.valery1707.interview.lunchVote.domain.TestEntity1;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/entity")
public class TestEntityController extends BaseEntityController<TestEntity1, TestEntityRepo> {
}
