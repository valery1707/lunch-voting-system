package name.valery1707.interview.lunchVote.configuration;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isAllUpperCase;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;
import static org.hibernate.boot.model.naming.Identifier.toIdentifier;

@SuppressWarnings("unused")
public class DatabaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {
	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		if (isAllUpperCase(name.getText())) {
			return super.toPhysicalColumnName(name, context);
		}
		String nameWithUnderscore = Stream.of(splitByCharacterTypeCamelCase(name.getText()))
				.map(StringUtils::uncapitalize)
				.collect(joining("_"));
		return super.toPhysicalColumnName(toIdentifier(nameWithUnderscore, name.isQuoted()), context);
	}
}
