package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerRoleDao extends AbstractDao<PractitionerRole>
{
	public PractitionerRoleDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, PractitionerRole.class, "practitioner_roles", "practitioner_role",
				"practitioner_role_id");
	}

	@Override
	protected PractitionerRole copy(PractitionerRole resource)
	{
		return resource.copy();
	}

	public PartialResult<PractitionerRole> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
