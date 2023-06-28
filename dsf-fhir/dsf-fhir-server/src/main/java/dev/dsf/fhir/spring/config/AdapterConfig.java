package dev.dsf.fhir.spring.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.dsf.fhir.adapter.FhirAdapter;
import dev.dsf.fhir.adapter.HtmlFhirAdapter;
import dev.dsf.fhir.adapter.QuestionnaireResponseHtmlGenerator;
import dev.dsf.fhir.adapter.TaskHtmlGenerator;

@Configuration
public class AdapterConfig
{
	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public FhirAdapter fhirAdapter()
	{
		return new FhirAdapter(fhirConfig.fhirContext());
	}

	@Bean
	public HtmlFhirAdapter htmlFhirAdapter()
	{
		return new HtmlFhirAdapter(fhirConfig.fhirContext(), () -> propertiesConfig.getServerBaseUrl(),
				List.of(new QuestionnaireResponseHtmlGenerator(), new TaskHtmlGenerator()));
	}
}
