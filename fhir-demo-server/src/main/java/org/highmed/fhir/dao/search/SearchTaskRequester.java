package org.highmed.fhir.dao.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

public class SearchTaskRequester implements SearchQuery
{
	private final IdType requester;

	public SearchTaskRequester(String requester)
	{
		this.requester = requester == null || requester.isBlank() ? null : new IdType(requester);
	}

	@Override
	public boolean isDefined()
	{
		return requester != null;
	}

	@Override
	public String getSubquery()
	{
		return "task->'requester'->>'reference' " + (requester.hasVersionIdPart() ? "=" : "LIKE") + " ?";
	}

	@Override
	public int getSqlParameterCount()
	{
		return 1;
	}

	@Override
	public void modifyStatement(int parameterIndex, PreparedStatement statement) throws SQLException
	{
		statement.setString(parameterIndex, requester.getValue() + (requester.hasVersionIdPart() ? "" : "%"));
	}

	@Override
	public CapabilityStatementRestResourceSearchParamComponent createCapabilityStatementPart()
	{
		return new CapabilityStatementRestResourceSearchParamComponent().setName("requester")
				.setDefinition("http://hl7.org/fhir/SearchParameter/Task-requester").setType(SearchParamType.REFERENCE)
				.setDocumentation("Search by task requester");
	}
}
