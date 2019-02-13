package org.highmed.fhir.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.PatientDao;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.rest.api.Constants;

@Path(PatientService.RESOURCE_TYPE_NAME)
public class PatientService extends AbstractService<PatientDao, Patient>
{
	public static final String RESOURCE_TYPE_NAME = "Patient";

	public PatientService(String serverBase, int defaultPageCount, PatientDao patientDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, patientDao);
	}
	
	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("page") Integer page, @QueryParam("_count") Integer count, @QueryParam("_format") String format,
			@Context UriInfo uri)
	{
		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? getDefaultPageCount() : count;

		PartialResult<Patient> patients = handleSql(() -> getDao().search(effectivePage, effectiveCount));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();

		return response(Status.OK, createSearchSet(patients, bundleUri, format), toSpecialMimeType(format)).build();
	}
}
