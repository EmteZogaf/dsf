package org.highmed.dsf.fhir.dao.provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.BundleDao;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.GroupDao;
import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.dao.LibraryDao;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.dao.MeasureReportDao;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.dao.ReadAccessDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class DaoProviderImpl implements DaoProvider, InitializingBean
{
	private final DataSource dataSource;
	private final ActivityDefinitionDao activityDefinitionDao;
	private final BinaryDao binaryDao;
	private final BundleDao bundleDao;
	private final CodeSystemDao codeSystemDao;
	private final EndpointDao endpointDao;
	private final GroupDao groupDao;
	private final HealthcareServiceDao healthcareServiceDao;
	private final LibraryDao libraryDao;
	private final LocationDao locationDao;
	private final MeasureDao measureDao;
	private final MeasureReportDao measureReportDao;
	private final NamingSystemDao namingSystemDao;
	private final OrganizationDao organizationDao;
	private final OrganizationAffiliationDao organizationAffiliationDao;
	private final PatientDao patientDao;
	private final PractitionerDao practitionerDao;
	private final PractitionerRoleDao practitionerRoleDao;
	private final ProvenanceDao provenanceDao;
	private final ResearchStudyDao researchStudyDao;
	private final StructureDefinitionDao structureDefinitionDao;
	private final StructureDefinitionDao structureDefinitionSnapshotDao;
	private final SubscriptionDao subscriptionDao;
	private final TaskDao taskDao;
	private final ValueSetDao valueSetDao;

	private final ReadAccessDao readAccessDao;

	private final Map<Class<? extends Resource>, ResourceDao<?>> daosByResourecClass = new HashMap<>();
	private final Map<String, ResourceDao<?>> daosByResourceTypeName = new HashMap<>();

	public DaoProviderImpl(DataSource dataSource, ActivityDefinitionDao activityDefinitionDao, BinaryDao binaryDao,
			BundleDao bundleDao, CodeSystemDao codeSystemDao, EndpointDao endpointDao, GroupDao groupDao,
			HealthcareServiceDao healthcareServiceDao, LibraryDao libraryDao, LocationDao locationDao,
			MeasureDao measureDao, MeasureReportDao measureReportDao, NamingSystemDao namingSystemDao,
			OrganizationDao organizationDao, OrganizationAffiliationDao organizationAffiliationDao,
			PatientDao patientDao, PractitionerDao practitionerDao, PractitionerRoleDao practitionerRoleDao,
			ProvenanceDao provenanceDao, ResearchStudyDao researchStudyDao,
			StructureDefinitionDao structureDefinitionDao, StructureDefinitionDao structureDefinitionSnapshotDao,
			SubscriptionDao subscriptionDao, TaskDao taskDao, ValueSetDao valueSetDao, ReadAccessDao readAccessDao)
	{
		this.dataSource = dataSource;
		this.activityDefinitionDao = activityDefinitionDao;
		this.binaryDao = binaryDao;
		this.bundleDao = bundleDao;
		this.codeSystemDao = codeSystemDao;
		this.endpointDao = endpointDao;
		this.groupDao = groupDao;
		this.healthcareServiceDao = healthcareServiceDao;
		this.libraryDao = libraryDao;
		this.locationDao = locationDao;
		this.measureDao = measureDao;
		this.measureReportDao = measureReportDao;
		this.namingSystemDao = namingSystemDao;
		this.organizationDao = organizationDao;
		this.organizationAffiliationDao = organizationAffiliationDao;
		this.patientDao = patientDao;
		this.practitionerDao = practitionerDao;
		this.practitionerRoleDao = practitionerRoleDao;
		this.provenanceDao = provenanceDao;
		this.researchStudyDao = researchStudyDao;
		this.structureDefinitionDao = structureDefinitionDao;
		this.structureDefinitionSnapshotDao = structureDefinitionSnapshotDao;
		this.subscriptionDao = subscriptionDao;
		this.taskDao = taskDao;
		this.valueSetDao = valueSetDao;

		this.readAccessDao = readAccessDao;

		daosByResourecClass.put(ActivityDefinition.class, activityDefinitionDao);
		daosByResourecClass.put(Binary.class, binaryDao);
		daosByResourecClass.put(Bundle.class, bundleDao);
		daosByResourecClass.put(CodeSystem.class, codeSystemDao);
		daosByResourecClass.put(Endpoint.class, endpointDao);
		daosByResourecClass.put(Group.class, groupDao);
		daosByResourecClass.put(HealthcareService.class, healthcareServiceDao);
		daosByResourecClass.put(Library.class, libraryDao);
		daosByResourecClass.put(Location.class, locationDao);
		daosByResourecClass.put(Measure.class, measureDao);
		daosByResourecClass.put(MeasureReport.class, measureReportDao);
		daosByResourecClass.put(NamingSystem.class, namingSystemDao);
		daosByResourecClass.put(Organization.class, organizationDao);
		daosByResourecClass.put(OrganizationAffiliation.class, organizationAffiliationDao);
		daosByResourecClass.put(Patient.class, patientDao);
		daosByResourecClass.put(Practitioner.class, practitionerDao);
		daosByResourecClass.put(PractitionerRole.class, practitionerRoleDao);
		daosByResourecClass.put(Provenance.class, provenanceDao);
		daosByResourecClass.put(ResearchStudy.class, researchStudyDao);
		daosByResourecClass.put(StructureDefinition.class, structureDefinitionDao);
		daosByResourecClass.put(Subscription.class, subscriptionDao);
		daosByResourecClass.put(Task.class, taskDao);
		daosByResourecClass.put(ValueSet.class, valueSetDao);

		daosByResourecClass.forEach((k, v) -> daosByResourceTypeName.put(k.getAnnotation(ResourceDef.class).name(), v));
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(activityDefinitionDao, "activityDefinitionDao");
		Objects.requireNonNull(binaryDao, "binaryDao");
		Objects.requireNonNull(bundleDao, "bundleDao");
		Objects.requireNonNull(codeSystemDao, "codeSystemDao");
		Objects.requireNonNull(endpointDao, "endpointDao");
		Objects.requireNonNull(groupDao, "groupDao");
		Objects.requireNonNull(healthcareServiceDao, "healthcareServiceDao");
		Objects.requireNonNull(libraryDao, "libraryDao");
		Objects.requireNonNull(locationDao, "locationDao");
		Objects.requireNonNull(measureDao, "measureDao");
		Objects.requireNonNull(measureReportDao, "measureReportDao");
		Objects.requireNonNull(namingSystemDao, "namingSystemDao");
		Objects.requireNonNull(organizationDao, "organizationDao");
		Objects.requireNonNull(organizationAffiliationDao, "organizationAffiliationDao");
		Objects.requireNonNull(patientDao, "patientDao");
		Objects.requireNonNull(practitionerDao, "practitionerDao");
		Objects.requireNonNull(practitionerRoleDao, "practitionerRoleDao");
		Objects.requireNonNull(provenanceDao, "provenanceDao");
		Objects.requireNonNull(researchStudyDao, "researchStudyDao");
		Objects.requireNonNull(structureDefinitionDao, "structureDefinitionDao");
		Objects.requireNonNull(structureDefinitionSnapshotDao, "structureDefinitionSnapshotDao");
		Objects.requireNonNull(subscriptionDao, "subscriptionDao");
		Objects.requireNonNull(taskDao, "taskDao");
		Objects.requireNonNull(valueSetDao, "valueSetDao");
	}

	@Override
	public Connection newReadOnlyAutoCommitTransaction() throws SQLException
	{
		Connection connection = dataSource.getConnection();

		if (!connection.isReadOnly() || !connection.getAutoCommit())
			throw new IllegalStateException("read only, auto commit connection expected from data source");

		return connection;
	}

	@Override
	public Connection newReadWriteTransaction() throws SQLException
	{
		Connection connection = dataSource.getConnection();
		connection.setReadOnly(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		connection.setAutoCommit(false);

		return connection;
	}

	@Override
	public ActivityDefinitionDao getActivityDefinitionDao()
	{
		return activityDefinitionDao;
	}

	@Override
	public BinaryDao getBinaryDao()
	{
		return binaryDao;
	}

	@Override
	public BundleDao getBundleDao()
	{
		return bundleDao;
	}

	@Override
	public CodeSystemDao getCodeSystemDao()
	{
		return codeSystemDao;
	}

	@Override
	public EndpointDao getEndpointDao()
	{
		return endpointDao;
	}

	@Override
	public GroupDao getGroupDao()
	{
		return groupDao;
	}

	@Override
	public HealthcareServiceDao getHealthcareServiceDao()
	{
		return healthcareServiceDao;
	}

	@Override
	public LibraryDao getLibraryDao()
	{
		return libraryDao;
	}

	@Override
	public LocationDao getLocationDao()
	{
		return locationDao;
	}

	@Override
	public MeasureDao getMeasureDao()
	{
		return measureDao;
	}

	@Override
	public MeasureReportDao getMeasureReportDao()
	{
		return measureReportDao;
	}

	@Override
	public NamingSystemDao getNamingSystemDao()
	{
		return namingSystemDao;
	}

	@Override
	public OrganizationDao getOrganizationDao()
	{
		return organizationDao;
	}

	@Override
	public OrganizationAffiliationDao getOrganizationAffiliationDao()
	{
		return organizationAffiliationDao;
	}

	@Override
	public PatientDao getPatientDao()
	{
		return patientDao;
	}

	@Override
	public PractitionerDao getPractitionerDao()
	{
		return practitionerDao;
	}

	@Override
	public PractitionerRoleDao getPractitionerRoleDao()
	{
		return practitionerRoleDao;
	}

	@Override
	public ProvenanceDao getProvenanceDao()
	{
		return provenanceDao;
	}

	@Override
	public ResearchStudyDao getResearchStudyDao()
	{
		return researchStudyDao;
	}

	@Override
	public StructureDefinitionDao getStructureDefinitionDao()
	{
		return structureDefinitionDao;
	}

	@Override
	public StructureDefinitionDao getStructureDefinitionSnapshotDao()
	{
		return structureDefinitionSnapshotDao;
	}

	@Override
	public SubscriptionDao getSubscriptionDao()
	{
		return subscriptionDao;
	}

	@Override
	public TaskDao getTaskDao()
	{
		return taskDao;
	}

	@Override
	public ValueSetDao getValueSetDao()
	{
		return valueSetDao;
	}

	@Override
	public <R extends Resource> Optional<? extends ResourceDao<R>> getDao(Class<R> resourceClass)
	{
		@SuppressWarnings("unchecked")
		ResourceDao<R> value = (ResourceDao<R>) daosByResourecClass.get(resourceClass);
		return Optional.ofNullable(value);
	}

	@Override
	public Optional<ResourceDao<?>> getDao(String resourceTypeName)
	{
		ResourceDao<?> value = daosByResourceTypeName.get(resourceTypeName);
		return Optional.ofNullable(value);
	}

	@Override
	public ReadAccessDao getReadAccessDao()
	{
		return readAccessDao;
	}
}
