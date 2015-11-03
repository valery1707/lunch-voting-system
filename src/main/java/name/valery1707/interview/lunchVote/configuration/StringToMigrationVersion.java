package name.valery1707.interview.lunchVote.configuration;

import org.flywaydb.core.api.MigrationVersion;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMigrationVersion implements Converter<String, MigrationVersion> {

	@Override
	public MigrationVersion convert(String source) {
		return MigrationVersion.fromVersion(source);
	}
}
