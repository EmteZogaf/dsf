package dev.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class DocumentReferenceXmlFhirAdapter extends XmlFhirAdapter<DocumentReference>
{
	public DocumentReferenceXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, DocumentReference.class);
	}
}
