package dev.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.hl7.fhir.r4.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.dao.EndpointDao;
import dev.dsf.fhir.search.filter.EndpointIdentityFilter;
import dev.dsf.fhir.search.parameters.EndpointAddress;
import dev.dsf.fhir.search.parameters.EndpointIdentifier;
import dev.dsf.fhir.search.parameters.EndpointName;
import dev.dsf.fhir.search.parameters.EndpointOrganization;
import dev.dsf.fhir.search.parameters.EndpointStatus;
import dev.dsf.fhir.search.parameters.rev.include.OrganizationEndpointRevInclude;

public class EndpointDaoJdbc extends AbstractResourceDaoJdbc<Endpoint> implements EndpointDao
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointDaoJdbc.class);

	public EndpointDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Endpoint.class, "endpoints", "endpoint",
				"endpoint_id", EndpointIdentityFilter::new,
				Arrays.asList(
						factory(EndpointAddress.PARAMETER_NAME, EndpointAddress::new,
								EndpointAddress.getNameModifiers()),
						factory(EndpointIdentifier.PARAMETER_NAME, EndpointIdentifier::new,
								EndpointIdentifier.getNameModifiers()),
						factory(EndpointName.PARAMETER_NAME, EndpointName::new, EndpointName.getNameModifiers()),
						factory(EndpointOrganization.PARAMETER_NAME, EndpointOrganization::new,
								EndpointOrganization.getNameModifiers(), EndpointOrganization::new,
								EndpointOrganization.getIncludeParameterValues()),
						factory(EndpointStatus.PARAMETER_NAME, EndpointStatus::new, EndpointStatus.getNameModifiers())),
				Arrays.asList(factory(OrganizationEndpointRevInclude::new,
						OrganizationEndpointRevInclude.getRevIncludeParameterValues())));
	}

	@Override
	protected Endpoint copy(Endpoint resource)
	{
		return resource.copy();
	}

	@Override
	public boolean existsActiveNotDeletedByAddress(String address) throws SQLException
	{
		if (address == null || address.isBlank())
			return false;

		try (Connection connection = getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"SELECT count(*) FROM current_endpoints WHERE endpoint->>'address' = ? AND endpoint->>'status' = 'active'"))
		{
			statement.setString(1, address);

			logger.trace("Executing query '{}'", statement);
			try (ResultSet result = statement.executeQuery())
			{
				return result.next() && result.getInt(1) > 0;
			}
		}
	}
}
