package org.highmed.dsf.fhir.organization;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class OrganizationProviderImpl implements OrganizationProvider, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationProviderImpl.class);

	private final FhirWebserviceClientProvider clientProvider;
	private final String organizationIdentifierLocalValue;
	private final Identifier localIdentifier;

	public OrganizationProviderImpl(FhirWebserviceClientProvider clientProvider,
			String organizationIdentifierLocalValue)
	{
		this.clientProvider = clientProvider;
		this.organizationIdentifierLocalValue = organizationIdentifierLocalValue;

		localIdentifier = new Identifier().setSystem(getDefaultIdentifierSystem())
				.setValue(organizationIdentifierLocalValue);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(organizationIdentifierLocalValue, "organizationIdentifierLocalValue");
	}

	@Override
	public String getDefaultTypeSystem()
	{
		return CODESYSTEM_HIGHMED_ORGANIZATION_TYPE;
	}

	@Override
	public String getDefaultIdentifierSystem()
	{
		return ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
	}

	@Override
	public String getLocalIdentifierValue()
	{
		return organizationIdentifierLocalValue;
	}

	@Override
	public Identifier getLocalIdentifier()
	{
		return localIdentifier;
	}

	private Stream<Organization> searchForOrganizations(String system, String identifier)
	{
		return searchForOrganizations(system + "|" + identifier);
	}

	private Stream<Organization> searchForOrganizations(String identifierValue)
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().searchWithStrictHandling(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier",
						Collections.singletonList(identifierValue)));

		return resultSet.getEntry().stream().map(bundleEntry -> bundleEntry.getResource())
				.filter(resource -> resource instanceof Organization).map(organization -> (Organization) organization);
	}

	@Override
	public Optional<Organization> getOrganization(String identifier)
	{
		return getOrganization(getDefaultIdentifierSystem(), identifier);
	}

	@Override
	public Optional<Organization> getOrganization(String system, String identifier)
	{
		return searchForOrganizations(system, identifier).findFirst();
	}

	@Override
	public Organization getLocalOrganization()
	{
		return getOrganization(organizationIdentifierLocalValue).get();
	}

	@Override
	public List<Organization> getRemoteOrganizations()
	{
		return searchForOrganizations(getDefaultIdentifierSystem(), "")
				.filter(noIdentifierMatches(getDefaultIdentifierSystem(), organizationIdentifierLocalValue))
				.collect(Collectors.toList());
	}

	@Override
	public Stream<Organization> getOrganizationsByType(String type)
	{
		Bundle resultSet = clientProvider.getLocalWebserviceClient().searchWithStrictHandling(Organization.class,
				Map.of("active", Collections.singletonList("true"), "type",
						Collections.singletonList(getDefaultTypeSystem() + "|" + type)));

		return resultSet.getEntry().stream().map(Bundle.BundleEntryComponent::getResource)
				.filter(resource -> resource instanceof Organization).map(organization -> (Organization) organization);
	}

	@Override
	public List<Identifier> getRemoteIdentifiers()
	{
		return getRemoteOrganizations().stream()
				.map(organization -> organization.getIdentifier().stream()
						.filter(identifierWithSystem(getDefaultIdentifierSystem())).findFirst().orElse(null))
				.filter(identifier -> identifier != null).collect(Collectors.toList());
	}

	@Override
	public Optional<Identifier> getIdentifier(IdType organizationId)
	{
		if (!organizationId.hasBaseUrl() || clientProvider.getLocalBaseUrl().equals(organizationId.getBaseUrl()))
		{
			List<Identifier> identifiers = clientProvider.getLocalWebserviceClient()
					.read(Organization.class, organizationId.getIdPart()).getIdentifier();
			return identifiers.stream().filter(identifierWithSystem(getDefaultIdentifierSystem())).findFirst();
		}
		else
		{
			logger.warn("OrganizationId starts with baseUrl not equal to {}, but {}", clientProvider.getLocalBaseUrl(),
					organizationId.getBaseUrl());
			return Optional.empty();
		}
	}

	@Override
	public Stream<Organization> searchRemoteOrganizations(String searchParameterIdentifierValue)
	{
		return searchForOrganizations(searchParameterIdentifierValue)
				.filter(noIdentifierMatches(getDefaultIdentifierSystem(), organizationIdentifierLocalValue));
	}

	@Override
	public Stream<Identifier> searchRemoteOrganizationsIdentifiers(String searchParameterIdentifierValue)
	{
		return searchRemoteOrganizations(searchParameterIdentifierValue)
				.map(organization -> organization.getIdentifier().stream()
						.filter(identifierWithSystem(getDefaultIdentifierSystem())).findFirst().orElse(null))
				.filter(identifier -> identifier != null);
	}

	private static Predicate<? super Identifier> identifierWithSystem(String system)
	{
		return identifier -> system.equals(identifier.getSystem());
	}

	private static Predicate<? super Organization> noIdentifierMatches(String system, String value)
	{
		return organization -> !organization.getIdentifier().stream()
				.anyMatch(i -> system.equals(i.getSystem()) && value.equals(i.getValue()));
	}
}
