package org.highmed.dsf.fhir.profile;

import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.PROFILE_HIGHEMD_RESEARCH_STUDY;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchStudy.ResearchStudyStatus;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ResearchStudyProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-extension-participating-medic-0.4.0.xml", "highmed-extension-participating-ttp-0.4.0.xml",
					"highmed-research-study-0.4.0.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-organization-type-0.4.0.xml"),
			Arrays.asList("highmed-authorization-role-0.4.0.xml", "highmed-organization-type-0.4.0.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testResearchStudyProfileValid() throws Exception
	{
		ResearchStudy res = new ResearchStudy();
		res.getMeta().addProfile(PROFILE_HIGHEMD_RESEARCH_STUDY);
		res.getIdentifierFirstRep().setSystem(NAMINGSYSTEM_HIGHMED_RESEARCH_STUDY_IDENTIFIER)
				.setValue(UUID.randomUUID().toString());
		res.setStatus(ResearchStudyStatus.ACTIVE);
		res.addEnrollment().setReference("Group/" + UUID.randomUUID().toString());
		Reference medicRef1 = new Reference().setType(ResourceType.Organization.name());
		medicRef1.getIdentifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 1");
		res.addExtension(EXTENSION_HIGHMED_PARTICIPATING_MEDIC, medicRef1);
		Reference medicRef2 = new Reference().setType(ResourceType.Organization.name());
		medicRef2.getIdentifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("MeDIC 2");
		res.addExtension(EXTENSION_HIGHMED_PARTICIPATING_MEDIC, medicRef2);
		Reference ttpRef = new Reference().setType(ResourceType.Organization.name());
		ttpRef.getIdentifier().setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue("TTP");
		res.addExtension(EXTENSION_HIGHMED_PARTICIPATING_TTP, ttpRef);

		ValidationResult result = resourceValidator.validate(res);
		result.getMessages().stream()
				.map(m -> m.getLocationString() + " " + m.getLocationLine() + ":" + m.getLocationCol() + " - " + m
						.getSeverity() + ": " + m.getMessage()).forEach(logger::info);

		assertEquals(0, result.getMessages().stream()
				.filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity()) || ResultSeverityEnum.FATAL
						.equals(m.getSeverity())).count());
	}
}
