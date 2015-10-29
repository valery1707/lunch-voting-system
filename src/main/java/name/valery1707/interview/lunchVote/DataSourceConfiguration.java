package name.valery1707.interview.lunchVote;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceConfiguration {
	@Value("${spring.datasource.driverClassName}")
	private String driverClassName;

	@Value("${spring.datasource.url}")
	private String url;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.username}")
	private String password;

	@Bean
	public DataSource dataSource() {
		PoolProperties poolProperties = new PoolProperties();
		poolProperties.setDriverClassName(driverClassName);
		poolProperties.setUrl(url);
		poolProperties.setUsername(username);
		poolProperties.setPassword(password);
		return new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Value("${flyway.locations}")
	private String flywayLocations;
	@Value("${flyway.encoding}")
	private String flywayEncoding;
	@Value("${flyway.schema}")
	private String flywaySchema;
	@Value("${flyway.sqlMigrationPrefix}")
	private String flywaySqlMigrationPrefix;
	@Value("${flyway.sqlMigrationSeparator}")
	private String flywaySqlMigrationSeparator;
	@Value("${flyway.sqlMigrationSuffix}")
	private String flywaySqlMigrationSuffix;
	@Value("${flyway.outOfOrder}")
	private boolean flywayOutOfOrder;
	@Value("${flyway.baselineOnMigrate}")
	private boolean flywayBaselineOnMigrate;
	@Value("${flyway.baselineVersion}")
	private String flywayBaselineVersion;
	@Value("${flyway.repairBeforeMigrate}")
	private boolean flywayRepairBeforeMigrate;

	@Bean
	public Flyway databaseMigration(DataSource dataSource) {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setLocations(flywayLocations.split(";"));
		flyway.setEncoding(flywayEncoding);
		flyway.setSqlMigrationPrefix(flywaySqlMigrationPrefix);
		flyway.setSqlMigrationSeparator(flywaySqlMigrationSeparator);
		flyway.setSqlMigrationSuffix(flywaySqlMigrationSuffix);
		flyway.setOutOfOrder(flywayOutOfOrder);
		flyway.setBaselineOnMigrate(flywayBaselineOnMigrate);
		flyway.setBaselineVersionAsString(flywayBaselineVersion);
		flyway.setSchemas(flywaySchema);

		if (flywayRepairBeforeMigrate) {
			flyway.repair();
		}
		flyway.migrate();
		return flyway;
	}
}
