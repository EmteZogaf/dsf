package dev.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.OrganizationAffiliation;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OrganizationAffiliationHtmlFhirAdapter extends HtmlFhirAdapter<OrganizationAffiliation>
{
	public OrganizationAffiliationHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, OrganizationAffiliation.class);
	}
}
