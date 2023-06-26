package dev.dsf.bpe;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

import dev.dsf.common.documentation.Documentation;
import dev.dsf.tools.db.DbMigratorConfig;
import dev.dsf.tools.docker.secrets.DockerSecretsPropertySourceFactory;

@Configuration
@PropertySource(value = "file:conf/config.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class BpeDbMigratorConfig implements DbMigratorConfig
{
	private static final String DB_LIQUIBASE_USER = "db.liquibase_user";
	private static final String DB_SERVER_USERS_GROUP = "db.server_users_group";
	private static final String DB_SERVER_USER = "db.server_user";
	private static final String DB_SERVER_USER_PASSWORD = "db.server_user_password";
	private static final String DB_CAMUNDA_USERS_GROUP = "db.camunda_users_group";
	private static final String DB_CAMUNDA_USER = "db.camunda_user";
	private static final String DB_CAMUNDA_USER_PASSWORD = "db.camunda_user_password";

	// Documentation in dsf-bpe-server/src/main/java/dev/dsf/bpe/spring/config/PropertiesConfig.java
	@Value("${dev.dsf.bpe.db.url}")
	private String dbUrl;

	@Documentation(description = "The user name to access the database from the DSF BPE server to execute database migrations")
	@Value("${dev.dsf.bpe.db.liquibase.username:liquibase_user}")
	private String dbLiquibaseUsername;

	@Documentation(required = true, description = "The password to access the database from the DSF BPE server to execute database migrations", recommendation = "Use docker secret file to configure by using *${env_variable}_FILE*", example = "/run/secrets/db_liquibase.password")
	@Value("${dev.dsf.bpe.db.liquibase.password}")
	private char[] dbLiquibasePassword;

	@Documentation(description = "The name of the user group to access the database from the DSF BPE server")
	@Value("${dev.dsf.bpe.db.user.group:bpe_users}")
	private String dbUsersGroup;

	// Documentation in dsf-bpe-server/src/main/java/dev/dsf/bpe/spring/config/PropertiesConfig.java
	@Value("${dev.dsf.bpe.db.user.username:bpe_server_user}")
	private String dbUsername;

	// Documentation in dsf-bpe-server/src/main/java/dev/dsf/bpe/spring/config/PropertiesConfig.java
	@Value("${dev.dsf.bpe.db.user.password}")
	private char[] dbPassword;

	@Documentation(description = "To force liquibase to unlock the migration lock set to `true`", recommendation = "Only use this option temporarily to unlock a stuck DB migration step")
	@Value("${dev.dsf.bpe.db.liquibase.forceUnlock:false}")
	private boolean dbLiquibaseUnlock;

	@Documentation(description = "Liquibase change lock wait time in minutes, default 2 minutes")
	@Value("${dev.dsf.bpe.db.liquibase.lockWaitTime:2}")
	private long dbLiquibaseLockWaitTime;

	@Documentation(description = "The name of the user group to access the database from the DSF BPE server for camunda processes")
	@Value("${dev.dsf.bpe.db.user.camunda.group:camunda_users}")
	private String dbCamundaUsersGroup;

	// Documentation in dsf-bpe-server/src/main/java/dev/dsf/bpe/spring/config/PropertiesConfig.java
	@Value("${dev.dsf.bpe.db.user.camunda.username:camunda_server_user}")
	private String dbCamundaUsername;

	// Documentation in dsf-bpe-server/src/main/java/dev/dsf/bpe/spring/config/PropertiesConfig.java
	@Value("${dev.dsf.bpe.db.user.camunda.password}")
	private char[] dbCamundaPassword;

	@Bean // static in order to initialize before @Configuration classes
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
			ConfigurableEnvironment environment)
	{
		new DockerSecretsPropertySourceFactory(environment).readDockerSecretsAndAddPropertiesToEnvironment();

		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	public String getDbUrl()
	{
		return dbUrl;
	}

	@Override
	public String getDbLiquibaseUsername()
	{
		return dbLiquibaseUsername;
	}

	@Override
	public char[] getDbLiquibasePassword()
	{
		return dbLiquibasePassword;
	}

	@Override
	public Map<String, String> getChangeLogParameters()
	{
		return Map.of(DB_LIQUIBASE_USER, dbLiquibaseUsername, DB_SERVER_USERS_GROUP, dbUsersGroup, DB_SERVER_USER,
				dbUsername, DB_SERVER_USER_PASSWORD, toString(dbPassword), DB_CAMUNDA_USERS_GROUP, dbCamundaUsersGroup,
				DB_CAMUNDA_USER, dbCamundaUsername, DB_CAMUNDA_USER_PASSWORD, toString(dbCamundaPassword));
	}

	private String toString(char[] password)
	{
		return password == null ? null : String.valueOf(password);
	}

	@Override
	public boolean forceLiquibaseUnlock()
	{
		return dbLiquibaseUnlock;
	}

	@Override
	public long getLiquibaseLockWaitTime()
	{
		return dbLiquibaseLockWaitTime;
	}
}
