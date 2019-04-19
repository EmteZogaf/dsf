package org.highmed.fhir.dao.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.Patient.PatientLinkComponent;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Provenance.ProvenanceAgentComponent;
import org.hl7.fhir.r4.model.Provenance.ProvenanceEntityComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskRestrictionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceExtractor
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceExtractor.class);

	@SafeVarargs
	private Function<Reference, ResourceReference> toResourceReference(
			Class<? extends DomainResource>... referenceTypes)
	{
		return ref -> new ResourceReference(ref, Arrays.asList(referenceTypes));
	}

	@SafeVarargs
	private <R extends DomainResource> Stream<ResourceReference> getReference(R resource, Predicate<R> hasReference,
			Function<R, Reference> getReference, Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(resource)
				? Stream.of(getReference.apply(resource)).map(toResourceReference(referenceTypes))
				: Stream.empty();
	}

	@SafeVarargs
	private <R extends DomainResource> Stream<ResourceReference> getReferences(R resource, Predicate<R> hasReference,
			Function<R, List<Reference>> getReference, Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(resource)
				? Stream.of(getReference.apply(resource)).flatMap(List::stream).map(toResourceReference(referenceTypes))
				: Stream.empty();
	}

	@SafeVarargs
	private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementsReference(
			R resource, Predicate<R> hasBackboneElements, Function<R, List<E>> getBackboneElements,
			Predicate<E> hasReference, Function<E, Reference> getReference,
			Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElements.test(resource))
		{
			List<E> backboneElements = getBackboneElements.apply(resource);
			return backboneElements.stream().map(e -> getReference(e, hasReference, getReference, referenceTypes))
					.flatMap(Function.identity());
		}
		else
			return Stream.empty();
	}

	@SafeVarargs
	private <E extends BackboneElement> Stream<ResourceReference> getReference(E backboneElement,
			Predicate<E> hasReference, Function<E, Reference> getReference,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(backboneElement)
				? Stream.of(getReference.apply(backboneElement)).map(toResourceReference(referenceTypes))
				: Stream.empty();
	}

	@SafeVarargs
	private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference> getBackboneElementReferences(
			R resource, Predicate<R> hasBackboneElement, Function<R, E> getBackboneElement, Predicate<E> hasReference,
			Function<E, List<Reference>> getReference, Class<? extends DomainResource>... referenceTypes)
	{
		if (hasBackboneElement.test(resource))
		{
			E backboneElement = getBackboneElement.apply(resource);
			return getReferences(backboneElement, hasReference, getReference, referenceTypes);
		}
		else
			return Stream.empty();
	}

	// not needed yet
	// @SafeVarargs
	// private <R extends DomainResource, E extends BackboneElement> Stream<ResourceReference>
	// getBackboneElementsReferences(
	// R resource, Predicate<R> hasBackboneElements, Function<R, List<E>> getBackboneElements,
	// Predicate<E> hasReference, Function<E, List<Reference>> getReference,
	// Class<? extends DomainResource>... referenceTypes)
	// {
	// if (hasBackboneElements.test(resource))
	// {
	// List<E> backboneElements = getBackboneElements.apply(resource);
	// return backboneElements.stream().map(e -> getReferences(e, hasReference, getReference, referenceTypes))
	// .flatMap(Function.identity());
	// }
	// else
	// return Stream.empty();
	// }

	@SafeVarargs
	private <E extends BackboneElement> Stream<ResourceReference> getReferences(E backboneElement,
			Predicate<E> hasReference, Function<E, List<Reference>> getReference,
			Class<? extends DomainResource>... referenceTypes)
	{
		return hasReference.test(backboneElement) ? Stream.of(getReference.apply(backboneElement)).flatMap(List::stream)
				.map(toResourceReference(referenceTypes)) : Stream.empty();
	}

	private Stream<ResourceReference> getExtensionReferences(DomainResource resource)
	{
		return resource.getExtension().stream().filter(e -> e.getValue() instanceof Reference)
				.map(e -> (Reference) e.getValue()).map(toResourceReference());
	}

	@SafeVarargs
	private Stream<ResourceReference> concat(Stream<ResourceReference>... streams)
	{
		if (streams.length == 0)
			return Stream.empty();
		else if (streams.length == 1)
			return streams[0];
		else if (streams.length == 2)
			return Stream.concat(streams[0], streams[1]);
		else
			return Arrays.stream(streams).flatMap(Function.identity());
	}

	public Stream<ResourceReference> getReferences(DomainResource resource)
	{
		if (resource instanceof Endpoint)
			return getReferences((Endpoint) resource);
		else if (resource instanceof HealthcareService)
			return getReferences((HealthcareService) resource);
		else if (resource instanceof Location)
			return getReferences((Location) resource);
		else if (resource instanceof Organization)
			return getReferences((Organization) resource);
		else if (resource instanceof Patient)
			return getReferences((Patient) resource);
		else if (resource instanceof Practitioner)
			return getReferences((Practitioner) resource);
		else if (resource instanceof PractitionerRole)
			return getReferences((PractitionerRole) resource);
		else if (resource instanceof Provenance)
			return getReferences((Provenance) resource);
		else if (resource instanceof ResearchStudy)
			return getReferences((ResearchStudy) resource);
		else if (resource instanceof Task)
			return getReferences((Task) resource);
		else
		{
			logger.debug("Resource of type {} not supported by {}, returning extension references only",
					resource.getClass().getName(), ReferenceExtractor.class.getName());
			return getExtensionReferences(resource);
		}
	}

	public Stream<ResourceReference> getReferences(Endpoint resource)
	{
		if (resource == null)
			return Stream.empty();

		var managingOrganization = getReference(resource, Endpoint::hasManagingOrganization,
				Endpoint::getManagingOrganization, Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(managingOrganization, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(HealthcareService resource)
	{
		if (resource == null)
			return Stream.empty();

		var providedBy = getReference(resource, HealthcareService::hasProvidedBy, HealthcareService::getProvidedBy,
				Organization.class);
		var locations = getReferences(resource, HealthcareService::hasLocation, HealthcareService::getLocation,
				Location.class);
		var coverageAreas = getReferences(resource, HealthcareService::hasCoverageArea,
				HealthcareService::getCoverageArea, Location.class);
		var endpoints = getReferences(resource, HealthcareService::hasEndpoint, HealthcareService::getEndpoint,
				Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(providedBy, locations, coverageAreas, endpoints, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Location resource)
	{
		var managingOrganization = getReference(resource, Location::hasManagingOrganization,
				Location::getManagingOrganization, Organization.class);
		var partOf = getReference(resource, Location::hasPartOf, Location::getPartOf, Location.class);
		var endpoints = getReferences(resource, Location::hasEndpoint, Location::getEndpoint, Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(managingOrganization, partOf, endpoints, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Organization resource)
	{
		var partOf = getReference(resource, Organization::hasPartOf, Organization::getPartOf, Location.class);
		var endpoints = getReferences(resource, Organization::hasEndpoint, Organization::getEndpoint, Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(partOf, endpoints, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Patient resource)
	{
		var contacts_organization = getBackboneElementsReference(resource, Patient::hasContact, Patient::getContact,
				ContactComponent::hasOrganization, ContactComponent::getOrganization, Organization.class);
		var generalPractitioners = getReferences(resource, Patient::hasGeneralPractitioner,
				Patient::getGeneralPractitioner, Organization.class, Practitioner.class, PractitionerRole.class);
		var managingOrganization = getReference(resource, Patient::hasManagingOrganization,
				Patient::getManagingOrganization, Organization.class);
		var links_other = getBackboneElementsReference(resource, Patient::hasLink, Patient::getLink,
				PatientLinkComponent::hasOther, PatientLinkComponent::getOther, Patient.class, RelatedPerson.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(contacts_organization, generalPractitioners, managingOrganization, links_other,
				extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Practitioner resource)
	{
		var qualifications_issuer = getBackboneElementsReference(resource, Practitioner::hasQualification,
				Practitioner::getQualification, PractitionerQualificationComponent::hasIssuer,
				PractitionerQualificationComponent::getIssuer, Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(qualifications_issuer, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(PractitionerRole resource)
	{
		var practitioner = getReference(resource, PractitionerRole::hasPractitioner, PractitionerRole::getPractitioner,
				Practitioner.class);
		var organization = getReference(resource, PractitionerRole::hasOrganization, PractitionerRole::getOrganization,
				Organization.class);
		var locations = getReferences(resource, PractitionerRole::hasLocation, PractitionerRole::getLocation,
				Location.class);
		var healthcareServices = getReferences(resource, PractitionerRole::hasHealthcareService,
				PractitionerRole::getHealthcareService, HealthcareService.class);
		var endpoints = getReferences(resource, PractitionerRole::hasEndpoint, PractitionerRole::getEndpoint,
				Endpoint.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(practitioner, organization, locations, healthcareServices, endpoints, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Provenance resource)
	{
		var targets = getReferences(resource, Provenance::hasTarget, Provenance::getTarget);
		var location = getReference(resource, Provenance::hasLocation, Provenance::getLocation, Location.class);
		var agents_who = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasWho, ProvenanceAgentComponent::getWho, Practitioner.class,
				PractitionerRole.class, RelatedPerson.class, Patient.class, Device.class, Organization.class);
		var agents_onBehalfOf = getBackboneElementsReference(resource, Provenance::hasAgent, Provenance::getAgent,
				ProvenanceAgentComponent::hasOnBehalfOf, ProvenanceAgentComponent::getOnBehalfOf, Practitioner.class,
				PractitionerRole.class, RelatedPerson.class, Patient.class, Device.class, Organization.class);
		var entities_what = getBackboneElementsReference(resource, Provenance::hasEntity, Provenance::getEntity,
				ProvenanceEntityComponent::hasWhat, ProvenanceEntityComponent::getWhat);

		var extensionReferences = getExtensionReferences(resource);

		return concat(targets, location, agents_who, agents_onBehalfOf, entities_what, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(ResearchStudy resource)
	{
		var protocols = getReferences(resource, ResearchStudy::hasProtocol, ResearchStudy::getProtocol,
				PlanDefinition.class);
		var partOfs = getReferences(resource, ResearchStudy::hasPartOf, ResearchStudy::getPartOf, ResearchStudy.class);
		var enrollments = getReferences(resource, ResearchStudy::hasEnrollment, ResearchStudy::getEnrollment,
				Group.class);
		var sponsor = getReference(resource, ResearchStudy::hasSponsor, ResearchStudy::getSponsor, Organization.class);
		var principalInvestigator = getReference(resource, ResearchStudy::hasPrincipalInvestigator,
				ResearchStudy::getPrincipalInvestigator, Practitioner.class, PractitionerRole.class);
		var sites = getReferences(resource, ResearchStudy::hasSite, ResearchStudy::getSite, Location.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(protocols, partOfs, enrollments, sponsor, principalInvestigator, sites, extensionReferences);
	}

	public Stream<ResourceReference> getReferences(Task resource)
	{
		var basedOns = getReferences(resource, Task::hasBasedOn, Task::getBasedOn);
		var partOfs = getReferences(resource, Task::hasPartOf, Task::getPartOf, Task.class);
		var focus = getReference(resource, Task::hasFocus, Task::getFocus);
		var for_ = getReference(resource, Task::hasFor, Task::getFor);
		var encounter = getReference(resource, Task::hasEncounter, Task::getEncounter, Encounter.class);
		var requester = getReference(resource, Task::hasRequester, Task::getRequester, Device.class, Organization.class,
				Patient.class, Practitioner.class, PractitionerRole.class, RelatedPerson.class);
		var owner = getReference(resource, Task::hasOwner, Task::getOwner, Practitioner.class, PractitionerRole.class,
				Organization.class, CareTeam.class, HealthcareService.class, Patient.class, Device.class,
				RelatedPerson.class);
		var location = getReference(resource, Task::hasLocation, Task::getLocation, Location.class);
		var reasonReference = getReference(resource, Task::hasReasonReference, Task::getReasonReference);
		var insurance = getReferences(resource, Task::hasInsurance, Task::getInsurance, Coverage.class,
				ClaimResponse.class);
		var relevanteHistories = getReferences(resource, Task::hasRelevantHistory, Task::getRelevantHistory,
				Provenance.class);
		var restriction_recipiets = getBackboneElementReferences(resource, Task::hasRestriction, Task::getRestriction,
				TaskRestrictionComponent::hasRecipient, TaskRestrictionComponent::getRecipient, Patient.class,
				Practitioner.class, PractitionerRole.class, RelatedPerson.class, Group.class, Organization.class);

		var extensionReferences = getExtensionReferences(resource);

		return concat(basedOns, partOfs, focus, for_, encounter, requester, owner, location, reasonReference, insurance,
				relevanteHistories, restriction_recipiets, extensionReferences);
	}
}
