package dev.dsf.fhir.dao.jdbc;

import java.util.Arrays;
import java.util.Collections;

import javax.sql.DataSource;

import org.hl7.fhir.r4.model.MeasureReport;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.dao.MeasureReportDao;
import dev.dsf.fhir.search.filter.MeasureReportIdentityFilter;
import dev.dsf.fhir.search.parameters.MeasureReportIdentifier;

public class MeasureReportDaoJdbc extends AbstractResourceDaoJdbc<MeasureReport> implements MeasureReportDao
{
	public MeasureReportDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, MeasureReport.class, "measure_reports",
				"measure_report", "measure_report_id", MeasureReportIdentityFilter::new,
				Arrays.asList(factory(MeasureReportIdentifier.PARAMETER_NAME, MeasureReportIdentifier::new,
						MeasureReportIdentifier.getNameModifiers())),
				Collections.emptyList());
	}

	@Override
	protected MeasureReport copy(MeasureReport resource)
	{
		return resource.copy();
	}
}
