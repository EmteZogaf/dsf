package dev.dsf.bpe.v1.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.plugin.BpmnFileAndModel;
import dev.dsf.bpe.plugin.ProcessPlugin;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.ProcessPluginApiImpl;
import dev.dsf.bpe.v1.ProcessPluginDefinition;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.config.ProxyConfig;
import dev.dsf.bpe.v1.service.EndpointProvider;
import dev.dsf.bpe.v1.service.FhirWebserviceClientProvider;
import dev.dsf.bpe.v1.service.MailService;
import dev.dsf.bpe.v1.service.OrganizationProvider;
import dev.dsf.bpe.v1.service.QuestionnaireResponseHelper;
import dev.dsf.bpe.v1.service.TaskHelper;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.ObjectMapperFactory;
import dev.dsf.fhir.authorization.process.ProcessAuthorizationHelper;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;

public class ProcessPluginImplTest
{
	private static final class TestProcessPluginDefinition implements ProcessPluginDefinition
	{
		final Map<String, List<String>> fhirResources;
		final List<String> processModels;
		final String version;
		final List<Class<?>> springConfigurations;
		final LocalDate releaseDate;

		TestProcessPluginDefinition(Map<String, List<String>> fhirResources, List<String> processModels, String version,
				List<Class<?>> springConfigurations, LocalDate releaseDate)
		{
			this.fhirResources = fhirResources;
			this.processModels = processModels;
			this.version = version;
			this.springConfigurations = springConfigurations;
			this.releaseDate = releaseDate;
		}

		@Override
		public String getName()
		{
			return "test";
		}

		@Override
		public String getVersion()
		{
			return version;
		}

		@Override
		public LocalDate getReleaseDate()
		{
			return releaseDate;
		}

		@Override
		public List<Class<?>> getSpringConfigurations()
		{
			return springConfigurations;
		}

		@Override
		public List<String> getProcessModels()
		{
			return processModels;
		}

		@Override
		public Map<String, List<String>> getFhirResourcesByProcessId()
		{
			return fhirResources;
		}
	}

	@Configuration
	// Configuration may not be private, final
	public static class TestConfig
	{
		@Autowired
		private ProcessPluginApi processPluginApi;

		@Bean
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public TestService testService()
		{
			return new TestService(processPluginApi);
		}
	}

	private static final class TestService extends AbstractServiceDelegate
	{
		public TestService(ProcessPluginApi processPluginApi)
		{
			super(processPluginApi);
		}

		@Override
		protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
		{
			// test: do nothing
		}
	}

	private ProxyConfig proxyConfig = mock(ProxyConfig.class);
	private EndpointProvider endpointProvider = mock(EndpointProvider.class);
	private FhirContext fhirContext = FhirContext.forR4();
	private FhirWebserviceClientProvider fhirWebserviceClientProvider = mock(FhirWebserviceClientProvider.class);
	private MailService mailService = mock(MailService.class);
	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper(fhirContext);
	private OrganizationProvider organizationProvider = mock(OrganizationProvider.class);
	private QuestionnaireResponseHelper questionnaireResponseHelper = mock(QuestionnaireResponseHelper.class);
	private ProcessAuthorizationHelper processAuthorizationHelper = mock(ProcessAuthorizationHelper.class);
	private ReadAccessHelper readAccessHelper = mock(ReadAccessHelper.class);
	private TaskHelper taskHelper = mock(TaskHelper.class);

	private ProcessPluginApi processPluginApi = new ProcessPluginApiImpl(proxyConfig, endpointProvider, fhirContext,
			fhirWebserviceClientProvider, mailService, objectMapper, organizationProvider, processAuthorizationHelper,
			questionnaireResponseHelper, readAccessHelper, taskHelper);
	private ConfigurableEnvironment environment = new StandardEnvironment();

	@Test
	public void testInitializeAndValidateResourcesAllNull() throws Exception
	{
		var definition = createPluginDefinition(null, null, null, null, null);
		var plugin = createPlugin(definition, false);

		assertFalse(plugin.initializeAndValidateResources(null));
		try
		{
			plugin.getApplicationContext();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}

		try
		{
			plugin.getProcessModels();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
		try
		{
			plugin.getFhirResources();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
	}

	@Test
	public void testInitializeAndValidateResourcesEmptySpringConfigBpmnAndFhirResources() throws Exception
	{
		var definition = createPluginDefinition("1.0.0.0", LocalDate.now(), Collections.emptyList(),
				Collections.emptyList(), Collections.emptyMap());
		var plugin = createPlugin(definition, false);

		assertFalse(plugin.initializeAndValidateResources(null));
		try
		{
			plugin.getApplicationContext();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}

		try
		{
			plugin.getProcessModels();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
		try
		{
			plugin.getFhirResources();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
	}

	@Test
	public void testInitializeAndValidateResourcesNotExistingModelAndFhirResources() throws Exception
	{
		var definition = createPluginDefinition("1.0.0.0", LocalDate.now(), List.of(TestConfig.class),
				List.of("test-plugin/does_not_exist.bpmn"),
				Map.of("testorg_test", List.of("test-plugin/does_not_exist.xml")));
		var plugin = createPlugin(definition, false);

		assertFalse(plugin.initializeAndValidateResources(null));
		try
		{
			plugin.getApplicationContext();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}

		try
		{
			plugin.getProcessModels();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
		try
		{
			plugin.getFhirResources();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
	}

	@Test
	public void testInitializeAndValidateResourcesNotExistingFhirResources() throws Exception
	{
		var definition = createPluginDefinition("1.0.0.0", LocalDate.now(), List.of(TestConfig.class),
				List.of("test-plugin/test.bpmn"), Map.of("testorg_test", List.of("test-plugin/does_not_exist.xml")));
		var plugin = createPlugin(definition, false);

		assertFalse(plugin.initializeAndValidateResources(null));
		try
		{
			plugin.getApplicationContext();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}

		try
		{
			plugin.getProcessModels();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
		try
		{
			plugin.getFhirResources();
			fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
		}
	}

	@Test
	public void testInitializeAndValidateResources() throws Exception
	{
		var definition = createPluginDefinition("1.0.0.0", LocalDate.now(), List.of(TestConfig.class),
				List.of("test-plugin/test.bpmn"),
				Map.of("testorg_test", List.of("test-plugin/ActivityDefinition_test.xml")));
		var plugin = createPlugin(definition, false);

		assertTrue(plugin.initializeAndValidateResources("test.org"));
		assertNotNull(plugin.getApplicationContext());
		assertNotNull(plugin.getProcessModels());
		assertNotNull(plugin.getFhirResources());


		List<BpmnFileAndModel> models = plugin.getProcessModels();
		assertEquals(1, models.size());
		BpmnFileAndModel bpmnFileAndModel = models.get(0);
		BpmnModelInstance model = bpmnFileAndModel.getModel();
		assertNotNull(model);

		Collection<Process> processes = model.getModelElementsByType(Process.class);
		assertNotNull(processes);
		assertEquals(1, processes.size());
		Process process = processes.stream().findFirst().get();
		Collection<CamundaProperties> camundaPropertiesElements = process.getExtensionElements()
				.getChildElementsByType(CamundaProperties.class);
		assertNotNull(camundaPropertiesElements);
		assertEquals(1, camundaPropertiesElements.size());
		CamundaProperties camundaProperties = camundaPropertiesElements.stream().findFirst().get();
		Collection<CamundaProperty> camundaPropertyElements = camundaProperties.getCamundaProperties();
		assertNotNull(camundaPropertyElements);
		assertEquals(1, camundaPropertyElements.size());
		CamundaProperty property = camundaPropertyElements.stream().findFirst().get();
		assertEquals(ProcessPlugin.MODEL_ATTRIBUTE_PROCESS_API_VERSION, property.getCamundaName());
		assertEquals(plugin.getProcessPluginApiVersion(), property.getCamundaValue());
	}

	private ProcessPluginDefinition createPluginDefinition(String version, LocalDate releaseDate,
			List<Class<?>> springConfigurations, List<String> processModels, Map<String, List<String>> fhirResources)
	{
		return new TestProcessPluginDefinition(fhirResources, processModels, version, springConfigurations,
				releaseDate);
	}

	private ProcessPluginImpl createPlugin(ProcessPluginDefinition processPluginDefinition, boolean draft)
	{
		return new ProcessPluginImpl(processPluginDefinition, processPluginApi, draft, Paths.get("test.jar"),
				getClass().getClassLoader(), fhirContext, environment);
	}
}
