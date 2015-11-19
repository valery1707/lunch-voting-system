package name.valery1707.interview.lunchVote.configuration;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToMigrationVersionTest {

	@Test
	public void testConvert() throws Exception {
		MigrationVersion version = new StringToMigrationVersion().convert("1.1.1");
		assertThat(version).isNotNull();
		assertThat(version.getVersion()).isNotNull().isEqualTo("1.1.1");
	}
}
