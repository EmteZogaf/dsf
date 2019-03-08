package org.highmed.fhir.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.ws.rs.WebApplicationException;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

public class TestFhirJerseyClient
{
	public static void main(String[] args)
			throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
	{
		String keyStorePassword = "password";
		KeyStore keyStore = CertificateReader.fromPkcs12(
				Paths.get("../fhir-demo-cert-generator/cert/test-client_certificate.p12"), keyStorePassword);
		KeyStore trustStore = CertificateHelper.extractTrust(keyStore);

		FhirContext fhirContext = FhirContext.forR4();
		WebserviceClient client = new WebserviceClientJersey("https://localhost:8001/fhir", trustStore, keyStore,
				keyStorePassword, null, null, null, 0, 0, null, fhirContext);

		try
		{
			// Patient patient = new Patient();
			// patient.setIdElement(new IdType("Patient", UUID.randomUUID().toString(), "2"));
			// Patient createdPatient = client.create(patient);
			//
			// createdPatient.setGender(AdministrativeGender.FEMALE);
			// client.update(createdPatient);

			//
			// Organization organization = client.create(Organization.class,
			// new Organization().setName("Test Organization"));
			//
			// ResearchStudy researchStudy = client.create(ResearchStudy.class,
			// new ResearchStudy().setDescription("Test Research Study").setSponsor(new Reference(organization)));
			//
			// Task task = new Task();
			// task.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/DataSharingTask");
			// task.setRequester(new Reference(organization.getIdElement()));
			// task.setDescription("Organization reference with version");
			// task.setAuthoredOn(new Date());
			// task.setStatus(TaskStatus.REQUESTED);
			// task.setIntent(TaskIntent.ORDER);
			// Extension ext = task.addExtension();
			// ext.setUrl("http://hl7.org/fhir/StructureDefinition/workflow-researchStudy");
			// Reference researchStudyReference = new Reference(researchStudy);
			// ext.setValue(researchStudyReference);
			//
			// client.create(task);
			//
			// client.create(
			// new Task().setRequester(new Reference(organization.getIdElement().toVersionless()))
			// .setDescription("Organization reference without version"));
			//

			// CapabilityStatement conformance = client.getConformance();
			// System.out.println(fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(conformance));

			// StructureDefinition sD = fhirContext.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/profiles/extension-workflow-researchstudy.xml")));
			// client.create(sD);

			// StructureDefinition sD = fhirContext.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/profiles/task-highmed-0.0.2.xml")));
			// client.create(sD);

			// StructureDefinition sd = client
			// .generateSnapshot("http://highmed.org/fhir/StructureDefinition/DataSharingTask");
			// String xml = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// StructureDefinition diff = fhirContext.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/task-highmed-0.0.1.xml")));
			// StructureDefinition sd = client.generateSnapshot(diff);
			// String xml = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// StructureDefinition diff = fhirContext.newXmlParser().parseResource(StructureDefinition.class,
			// Files.newInputStream(Paths.get("../fhir-demo-server/src/test/resources/address-de-basis-0.2.xml")));
			// StructureDefinition sd = client.generateSnapshot(diff.setSnapshot(null));
			// String xml = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(sd);
			// System.out.println(xml);

			// Subscription subscription = new Subscription();
			// subscription.setStatus(SubscriptionStatus.ACTIVE);
			// subscription.setReason("Test");
			// subscription.setCriteria("Task");
			// SubscriptionChannelComponent channel = subscription.getChannel();
			// channel.setType(SubscriptionChannelType.WEBSOCKET);
			// channel.setPayload(Constants.CT_FHIR_JSON_NEW);
			//
			// client.create(subscription);

			// Task createdTask = client.create(new Task().setDescription("Status draft").setStatus(TaskStatus.DRAFT));
			//
			// createdTask.setStatus(TaskStatus.REQUESTED).setDescription("Status requested");
			// client.update(createdTask);

			// Organization org = client.read(Organization.class, "e8aa9c06-9789-4c2b-8292-1c2a9601c2cc");
			//
			// Endpoint endpoint = new Endpoint();
			// endpoint.setStatus(EndpointStatus.ACTIVE);
			// endpoint.setConnectionType(new Coding("http://terminology.hl7.org/CodeSystem/endpoint-connection-type",
			// "hl7-fhir-rest", "HL7 FHIR"));
			// endpoint.setManagingOrganization(
			// new Reference(new IdType(org.getIdElement().getResourceType(), org.getIdElement().getIdPart())));
			// endpoint.setAddress("https://localhost:8001/fhir");
			// endpoint.addPayloadType(new CodeableConcept(new Coding("http://hl7.org/fhir/resource-types", "Task",
			// "Task")));
			// endpoint.addPayloadMimeType(Constants.CT_FHIR_JSON_NEW);
			// endpoint.addPayloadMimeType(Constants.CT_FHIR_XML_NEW);
			//
			// Endpoint createdEndpoint = client.create(endpoint);
			//
			// org.getEndpoint().clear();
			// org.addEndpoint(new Reference(new IdType(createdEndpoint.getIdElement().getResourceType(),
			// createdEndpoint.getIdElement().getIdPart())));
			// client.update(org);

			// Organization org = client.read(Organization.class, "e8aa9c06-9789-4c2b-8292-1c2a9601c2cc");
			// org.setActive(true);
			// client.update(org);
		}
		catch (WebApplicationException e)
		{
			if (e.getResponse() != null && e.getResponse().hasEntity())
			{
				OperationOutcome outcome = e.getResponse().readEntity(OperationOutcome.class);
				String xml = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(outcome);
				System.out.println(xml);
			}
			else
				e.printStackTrace();
		}
	}
}
