package org.octri.fhir_sandbox.service;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octri.fhir_sandbox.config.FhirServerProperties;
import org.octri.fhir_sandbox.config.SandboxDataConfig;
import org.octri.fhir_sandbox.util.FhirDataUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;

@ExtendWith(MockitoExtension.class)
public class SampleDataServiceTest {

	@Mock
	SandboxDataConfig dataConfig;
	@Mock
	ResourcePatternResolver resourcePatternResolver;
	@Mock
	FhirServerProperties fhirServerProperties;
	@Mock
	FhirContext fhirContext;

	SampleDataService sampleDataService;

	private String fhirUrl = "http://localhost:8001/fhir/my-partition-id/";
	private IGenericClient mockClient = mock(IGenericClient.class);

	MockedStatic<FhirDataUtil> utilities;
	private String ancillaryDataPattern = "dependencies/*.json";
	private String dependentDataPattern = "data/*.json";
	private Resource[] ancillaryResources;
	private Resource[] dependentResources;
	private Bundle ancillaryBundle = mock(Bundle.class);

	@BeforeEach
	public void setup() {
		utilities = mockStatic(FhirDataUtil.class);

		// Manually mock service
		dataConfig = mock(SandboxDataConfig.class);
		resourcePatternResolver = mock(ResourcePatternResolver.class);
		fhirServerProperties = mock(FhirServerProperties.class);
		fhirContext = mock(FhirContext.class);
		// Socket timeout configuration requires addition stubbing
		when(fhirContext.getRestfulClientFactory()).thenReturn(mock(IRestfulClientFactory.class));
		sampleDataService = new SampleDataService(dataConfig, resourcePatternResolver, fhirServerProperties,
				fhirContext);

		// Mock data resources
		ancillaryResources = new Resource[1];
		ancillaryResources[0] = mock(Resource.class);
		dependentResources = new Resource[1];
		dependentResources[0] = mock(Resource.class);
		when(dataConfig.getSampleResourcePatterns()).thenReturn(List.of(ancillaryDataPattern, dependentDataPattern));
	}

	@AfterEach
	public void tearDown() {
		utilities.close();
	}

	@Test
	public void testExceptionReadingDependecyData() throws IOException {
		doThrow(new IOException()).when(resourcePatternResolver).getResources(ancillaryDataPattern);
		assertThrows(UncheckedIOException.class, () -> {
			sampleDataService.loadSampleData(fhirUrl);
		});
		utilities.verify(() -> FhirDataUtil.readFhirJson(dependentResources[0], Bundle.class),
				never().description("Exceptions in ancillary data should prevent processing of dependent data."));
	}

	@Test
	public void testExceptionParsingDependecyData() throws IOException {
		when(resourcePatternResolver.getResources(ancillaryDataPattern)).thenReturn(ancillaryResources);
		utilities.when(() -> FhirDataUtil.readFhirJson(ancillaryResources[0], Bundle.class))
				.thenThrow(new UncheckedIOException(new IOException()));
		assertThrows(UncheckedIOException.class, () -> {
			sampleDataService.loadSampleData(fhirUrl);
		});

		utilities.when(() -> FhirDataUtil.readFhirJson(ancillaryResources[0], Bundle.class))
				.thenThrow(new DataFormatException());
		assertThrows(DataFormatException.class, () -> {
			sampleDataService.loadSampleData(fhirUrl);
		});

		utilities.verify(() -> FhirDataUtil.readFhirJson(dependentResources[0], Bundle.class),
				never().description("Exceptions in ancillary data should prevent processing of dependent data."));
	}

	@Test
	public void testExceptionWritingDependencyData() throws IOException {
		when(resourcePatternResolver.getResources(ancillaryDataPattern)).thenReturn(ancillaryResources);
		when(fhirContext.newRestfulGenericClient(any())).thenReturn(mockClient);
		utilities.when(() -> FhirDataUtil.readFhirJson(ancillaryResources[0], Bundle.class))
				.thenReturn(ancillaryBundle);
		when(mockClient.transaction()).thenThrow(new FhirClientConnectionException("Connection Error"));

		assertThrows(FhirClientConnectionException.class, () -> {
			sampleDataService.loadSampleData(fhirUrl);
		});

		utilities.verify(() -> FhirDataUtil.readFhirJson(ancillaryResources[0], Bundle.class),
				description("Processing ancillary data should invoke readFhirJson"));
		utilities.verify(() -> FhirDataUtil.readFhirJson(dependentResources[0], Bundle.class),
				never().description("Exceptions in ancillary data should prevent processing of dependent data."));
	}
}
