package dev.dsf.bpe.spring.config;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.dsf.mpi.client.MasterPatientIndexClientFactory;
import dev.dsf.mpi.client.MasterPatientIndexClientServiceLoader;

@Configuration
public class MasterPatientIndexConfig
{
	private static final Logger logger = LoggerFactory.getLogger(MasterPatientIndexConfig.class);

	@Autowired
	private PropertiesConfig propertiesConfig;

	@Bean
	public MasterPatientIndexClientServiceLoader masterPatientIndexClientServiceLoader()
	{
		return new MasterPatientIndexClientServiceLoader();
	}

	@Bean
	public MasterPatientIndexClientFactory masterPatientIndexClientFactory()
	{
		MasterPatientIndexClientFactory factory = masterPatientIndexClientServiceLoader()
				.getMasterPatientIndexClientFactory(propertiesConfig.getMasterPatientIndexClientFactoryClass())
				.orElseThrow(() -> new NoSuchElementException("Master patient index client factory with classname='"
						+ propertiesConfig.getMasterPatientIndexClientFactoryClass() + "' not found"));

		if ("dev.dsf.mpi.client.stub.MasterPatientIndexClientStubFactory".equals(factory.getClass().getName()))
			logger.warn("Using {} as MPI client factory", factory.getClass().getName());
		else
			logger.info("Using {} as MPI client factory", factory.getClass().getName());

		return factory;
	}
}
