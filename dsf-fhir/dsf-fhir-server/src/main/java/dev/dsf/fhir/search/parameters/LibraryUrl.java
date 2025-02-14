package dev.dsf.fhir.search.parameters;

import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Resource;

import dev.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import dev.dsf.fhir.search.parameters.basic.AbstractUrlAndVersionParameter;

@SearchParameterDefinition(name = LibraryUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Library-url", type = SearchParamType.URI, documentation = "The uri that identifies the library")
public class LibraryUrl extends AbstractUrlAndVersionParameter<Library>
{
	public static final String RESOURCE_COLUMN = "library";

	public LibraryUrl()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof Library;
	}
}
