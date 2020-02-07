package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class SubscriptionHtmlFhirAdapter extends HtmlFhirAdapter<Subscription>
{
	public SubscriptionHtmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Subscription.class);
	}
}
