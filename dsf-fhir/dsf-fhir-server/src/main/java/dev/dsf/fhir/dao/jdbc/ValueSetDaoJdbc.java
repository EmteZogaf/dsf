package dev.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.dao.ValueSetDao;
import dev.dsf.fhir.search.parameters.ValueSetDate;
import dev.dsf.fhir.search.parameters.ValueSetIdentifier;
import dev.dsf.fhir.search.parameters.ValueSetStatus;
import dev.dsf.fhir.search.parameters.ValueSetUrl;
import dev.dsf.fhir.search.parameters.ValueSetVersion;
import dev.dsf.fhir.search.parameters.user.ValueSetUserFilter;

public class ValueSetDaoJdbc extends AbstractResourceDaoJdbc<ValueSet> implements ValueSetDao
{
	private final ReadByUrlDaoJdbc<ValueSet> readByUrl;

	public ValueSetDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, ValueSet.class, "value_sets", "value_set",
				"value_set_id", ValueSetUserFilter::new, with(ValueSetDate::new, ValueSetIdentifier::new,
						ValueSetStatus::new, ValueSetUrl::new, ValueSetVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected ValueSet copy(ValueSet resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
