package dev.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PatientJsonFhirAdapter extends JsonFhirAdapter<Patient>
{
	public PatientJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Patient.class);
	}
}
