package dev.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.search.parameters.user.StructureDefinitionUserFilter;

public class StructureDefinitionDaoJdbc extends AbstractStructureDefinitionDaoJdbc
{
	public StructureDefinitionDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, "structure_definitions", "structure_definition",
				"structure_definition_id", StructureDefinitionUserFilter::new);
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
