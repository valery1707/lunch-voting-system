package name.valery1707.interview.lunchVote.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import name.valery1707.interview.lunchVote.domain.IBaseEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Service
@Scope(scopeName = SCOPE_SINGLETON)
public class JsonMapper {
	private ObjectMapper mapper;

	@PostConstruct
	public void postConstruct() {
		//todo Use Spring inject
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	public <T extends IBaseEntity> String writeValueAsString(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Catch exception while marshall object to JSON: " + value, e);
		}
	}
}
