package dev.dsf.bpe.dao;

import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.Driver;
import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class AbstractDbTest
{
	static
	{
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	protected static final String CHANGE_LOG_FILE = "db/db.changelog.xml";

	protected static final String DATABASE_USERS_GROUP = "server_users_group";
	protected static final String DATABASE_USER = "server_user";
	protected static final String DATABASE_USER_PASSWORD = "server_user_password";

	protected static final String DATABASE_CAMUNDA_USERS_GROUP = "camunda_users_group";
	protected static final String DATABASE_CAMUNDA_USER = "camunda_user";
	protected static final String DATABASE_CAMUNDA_USER_PASSWORD = "camunda_user_password";

	protected static final String DATABASE_URL = "jdbc:postgresql://localhost:54321/db";

	protected static final Map<String, String> CHANGE_LOG_PARAMETERS = Map.of("db.liquibase_user", "postgres",
			"db.server_users_group", DATABASE_USERS_GROUP, "db.server_user", DATABASE_USER, "db.server_user_password",
			DATABASE_USER_PASSWORD, "db.camunda_users_group", DATABASE_CAMUNDA_USERS_GROUP, "db.camunda_user",
			DATABASE_CAMUNDA_USER, "db.camunda_user_password", DATABASE_CAMUNDA_USER_PASSWORD);

	public static BasicDataSource createDefaultDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(DATABASE_URL);
		dataSource.setUsername(DATABASE_USER);
		dataSource.setPassword(DATABASE_USER_PASSWORD);
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");

		return dataSource;
	}

	public static BasicDataSource createLiquibaseDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(DATABASE_URL);
		dataSource.setUsername("postgres");
		dataSource.setPassword("password");
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");

		return dataSource;
	}

	public static BasicDataSource createAdminBasicDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl("jdbc:postgresql://localhost:54321/postgres");
		dataSource.setUsername("postgres");
		dataSource.setPassword("password");

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");

		return dataSource;
	}

	public static BasicDataSource createCamundaDataSource()
	{
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(Driver.class.getName());
		dataSource.setUrl(DATABASE_URL);
		dataSource.setUsername(DATABASE_CAMUNDA_USER);
		dataSource.setPassword(DATABASE_CAMUNDA_USER_PASSWORD);
		dataSource.setDefaultReadOnly(true);

		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");

		return dataSource;
	}
}
