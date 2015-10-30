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
		return super.toPhysicalColumnName(camelCaseToUnderscores(name), context);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return super.toPhysicalTableName(camelCaseToUnderscores(name), context);
	}

	private static Identifier camelCaseToUnderscores(Identifier src) {
		if (isAllUpperCase(src.getText())) {
			return src;
		}
		String nameWithUnderscore = Stream.of(splitByCharacterTypeCamelCase(src.getText()))
				.filter(StringUtils::isNotBlank)
				.filter(s -> !"_".equals(s))
				.map(StringUtils::uncapitalize)
				.collect(joining("_"));
		return toIdentifier(nameWithUnderscore, src.isQuoted());
	}
}
