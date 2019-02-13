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

import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.rest.api.Constants;

@Path(LocationService.RESOURCE_TYPE_NAME)
public class LocationService extends AbstractService<LocationDao, Location>
{
	public static final String RESOURCE_TYPE_NAME = "Location";

	public LocationService(String serverBase, int defaultPageCount, LocationDao locationDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, locationDao);
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("page") Integer page, @QueryParam("_count") Integer count,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? getDefaultPageCount() : count;

		PartialResult<Location> patients = handleSql(() -> getDao().search(effectivePage, effectiveCount));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();

		return response(Status.OK, createSearchSet(patients, bundleUri, format), toSpecialMimeType(format)).build();
	}
}
