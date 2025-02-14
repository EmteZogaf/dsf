package dev.dsf.bpe.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.ValueSet;

import dev.dsf.bpe.v1.constants.NamingSystems.TaskIdentifier;

public final class ProcessesResource
{
	public static ProcessesResource from(Resource resource)
	{
		Objects.requireNonNull(resource, "resource");

		if (resource instanceof ActivityDefinition)
			return fromMetadataResource((ActivityDefinition) resource);
		else if (resource instanceof CodeSystem)
			return fromMetadataResource((CodeSystem) resource);
		else if (resource instanceof Library)
			return fromMetadataResource((Library) resource);
		else if (resource instanceof Measure)
			return fromMetadataResource((Measure) resource);
		else if (resource instanceof NamingSystem)
			return fromNamingSystem((NamingSystem) resource);
		else if (resource instanceof Questionnaire)
			return fromMetadataResource((Questionnaire) resource);
		else if (resource instanceof StructureDefinition)
			return fromMetadataResource((StructureDefinition) resource);
		else if (resource instanceof Task)
			return fromTask((Task) resource);
		else if (resource instanceof ValueSet)
			return fromMetadataResource((ValueSet) resource);
		else
			throw new IllegalArgumentException(
					"MetadataResource of type " + resource.getClass().getName() + " not supported");
	}

	public static ProcessesResource fromMetadataResource(MetadataResource resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType(), resource.getUrl(), resource.getVersion(), null, null),
				resource);
	}

	public static ProcessesResource fromNamingSystem(NamingSystem resource)
	{
		return new ProcessesResource(new ResourceInfo(resource.getResourceType(), null, null, resource.getName(), null),
				resource);
	}

	public static ProcessesResource fromTask(Task resource)
	{
		return new ProcessesResource(
				new ResourceInfo(resource.getResourceType(), null, null, null, getIdentifier(resource)), resource);
	}

	private static String getIdentifier(Task resource)
	{
		return TaskIdentifier.findFirst(resource).map(Identifier::getValue).get();
	}

	public static ProcessesResource from(ResourceInfo resourceInfo)
	{
		return new ProcessesResource(resourceInfo, null);
	}

	private final ResourceInfo resourceInfo;
	private final Resource resource;
	private final Set<ProcessIdAndVersion> processes = new HashSet<>();

	private ProcessState oldState;
	private ProcessState newState;

	private ProcessesResource(ResourceInfo resourceInfo, Resource resource)
	{
		this.resourceInfo = resourceInfo;
		this.resource = resource;
	}

	public ResourceInfo getResourceInfo()
	{
		return resourceInfo;
	}

	public Resource getResource()
	{
		return resource;
	}

	public Set<ProcessIdAndVersion> getProcesses()
	{
		return Collections.unmodifiableSet(processes);
	}

	public ProcessesResource add(ProcessIdAndVersion process)
	{
		processes.add(process);

		return this;
	}

	public void addAll(Set<ProcessIdAndVersion> processes)
	{
		this.processes.addAll(processes);
	}

	public ProcessesResource setOldProcessState(ProcessState oldState)
	{
		this.oldState = oldState;

		return this;
	}

	public ProcessState getOldProcessState()
	{
		return oldState;
	}

	public ProcessesResource setNewProcessState(ProcessState newState)
	{
		this.newState = newState;

		return this;
	}

	public ProcessState getNewProcessState()
	{
		return newState;
	}

	public boolean hasStateChangeOrDraft()
	{
		return !Objects.equals(getOldProcessState(), getNewProcessState())
				|| (ProcessState.DRAFT.equals(getOldProcessState()) && ProcessState.DRAFT.equals(getNewProcessState()));
	}

	public boolean notNewToExcludedChange()
	{
		return !(ProcessState.NEW.equals(getOldProcessState()) && ProcessState.EXCLUDED.equals(getNewProcessState()));
	}

	public boolean shouldExist()
	{
		return (ProcessState.ACTIVE.equals(getOldProcessState()) && ProcessState.ACTIVE.equals(getNewProcessState()))
				|| (ProcessState.RETIRED.equals(getOldProcessState())
						&& ProcessState.RETIRED.equals(getNewProcessState()));
	}

	public BundleEntryComponent toBundleEntry(String baseUrl)
	{
		switch (getOldProcessState())
		{
			case MISSING:
				return fromMissing();
			case NEW:
				return fromNew();
			case ACTIVE:
				return fromActive(baseUrl);
			case DRAFT:
				return fromDraft(baseUrl);
			case RETIRED:
				return fromRetired(baseUrl);
			case EXCLUDED:
				return fromExcluded();
			default:
				throw new RuntimeException(
						ProcessState.class.getSimpleName() + " " + getOldProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromMissing()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromNew()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case DRAFT:
				return createAsDraft();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromActive(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case DRAFT:
				return updateToDraft(baseUrl);
			case RETIRED:
				return updateToRetired(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromDraft(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(baseUrl);
			case DRAFT:
				return updateToDraft(baseUrl);
			case RETIRED:
				return updateToRetired(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromRetired(String baseUrl)
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return updateToActive(baseUrl);
			case DRAFT:
				return updateToDraft(baseUrl);
			case EXCLUDED:
				return delete();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent fromExcluded()
	{
		switch (getNewProcessState())
		{
			case ACTIVE:
				return createAsActive();
			case DRAFT:
				return createAsDraft();
			case RETIRED:
				return createAsRetired();
			default:
				throw new RuntimeException(
						"State change " + getOldProcessState() + " -> " + getNewProcessState() + " not supported");
		}
	}

	private BundleEntryComponent createAsActive()
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.ACTIVE);

		return create();
	}

	private BundleEntryComponent createAsDraft()
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.DRAFT);

		return create();
	}

	private BundleEntryComponent createAsRetired()
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.RETIRED);

		return create();
	}

	private BundleEntryComponent create()
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(getResource());
		entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.POST);
		request.setUrl(getResourceInfo().getResourceType().name());
		request.setIfNoneExist(getResourceInfo().toConditionalUrl());

		return entry;
	}

	private BundleEntryComponent updateToActive(String baseUrl)
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.ACTIVE);

		return update(baseUrl);
	}

	private BundleEntryComponent updateToDraft(String baseUrl)
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.DRAFT);

		return update(baseUrl);
	}

	private BundleEntryComponent updateToRetired(String baseUrl)
	{
		if (getResource() instanceof MetadataResource)
			((MetadataResource) getResource()).setStatus(PublicationStatus.RETIRED);

		return update(baseUrl);
	}

	private BundleEntryComponent update(String baseUrl)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setResource(getResource());
		entry.setFullUrl("urn:uuid:" + UUID.randomUUID().toString());

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.PUT);
		request.setUrl(getResourceInfo().getResourceType().name() + "?" + getResourceInfo().toConditionalUrl());

		return entry;
	}

	private BundleEntryComponent delete()
	{
		BundleEntryComponent entry = new BundleEntryComponent();

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.DELETE);
		request.setUrl(getResourceInfo().getResourceType().name() + "?" + getResourceInfo().toConditionalUrl());

		return entry;
	}

	public List<String> getExpectedStatus()
	{
		switch (getOldProcessState())
		{
			case MISSING:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case NEW:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case DRAFT:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case ACTIVE:
				switch (getNewProcessState())
				{
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case RETIRED:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case DRAFT:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// standard update with resource id
						return Collections.singletonList("200");
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case RETIRED:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case RETIRED:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// standard update with resource id
						return Collections.singletonList("200");
					case DRAFT:
						// standard update with resource id
						return Collections.singletonList("200");
					case EXCLUDED:
						// standard delete with resource id
						return Arrays.asList("200", "204");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			case EXCLUDED:
				switch (getNewProcessState())
				{
					case ACTIVE:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case DRAFT:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					case RETIRED:
						// conditional create NamingSystem: name=..., Task: identifier=..., others: url=...&version=...
						return Arrays.asList("200", "201");
					default:
						throw new RuntimeException("State change " + getOldProcessState() + " -> "
								+ getNewProcessState() + " not supported");
				}
			default:
				throw new RuntimeException(
						ProcessState.class.getSimpleName() + " " + getOldProcessState() + " not supported");
		}
	}

	public BundleEntryComponent toSearchBundleEntryCount0()
	{
		BundleEntryComponent entry = new BundleEntryComponent();

		BundleEntryRequestComponent request = entry.getRequest();
		request.setMethod(HTTPVerb.GET);
		request.setUrl(getSearchBundleEntryUrl() + "&_count=0");

		return entry;
	}

	public String getSearchBundleEntryUrl()
	{
		return getResourceInfo().getResourceType().name() + "?" + getResourceInfo().toConditionalUrl();
	}
}
