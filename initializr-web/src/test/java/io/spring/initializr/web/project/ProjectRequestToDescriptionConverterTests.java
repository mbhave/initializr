/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.web.project;

import io.spring.initializr.generator.BasicProjectRequest;
import io.spring.initializr.generator.InvalidProjectRequestException;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.util.Version;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ProjectRequestToDescriptionConverter}.
 *
 * @author Madhura Bhave
 */
public class ProjectRequestToDescriptionConverterTests {

	private ProjectRequestToDescriptionConverter converter;

	private ProjectRequestValidator validator = mock(ProjectRequestValidator.class);

	private InitializrMetadataProvider metadataProvider = mock(
			InitializrMetadataProvider.class);

	@BeforeEach
	void setup() {
		given(this.metadataProvider.get())
				.willReturn(InitializrMetadataTestBuilder.withDefaults().build());
		this.converter = new ProjectRequestToDescriptionConverter(this.validator,
				this.metadataProvider);
	}

	@Test
	void convertShouldValidateProjectRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		this.converter.convert(request);
		verify(this.validator).validate(request);
	}

	@Test
	void convertShouldSetApplicationNameForProjectDescriptionFromRequestWhenPresent() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setApplicationName("MyApplication");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getApplicationName()).isEqualTo("MyApplication");
	}

	@Test
	void convertShouldSetApplicationNameForProjectDescriptionUsingNameWhenAbsentFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getApplicationName()).isEqualTo("Application");
	}

	@Test
	void convertShouldSetGroupIdAndArtifactIdFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setArtifactId("foo");
		request.setGroupId("com.example");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getGroupId()).isEqualTo("com.example");
		assertThat(description.getArtifactId()).isEqualTo("foo");
	}

	@Test
	void convertShouldSetBaseDirectoryFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setBaseDir("my-path");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getBaseDirectory()).isEqualTo("my-path");
	}

	@Test
	void convertShouldSetBuildSystemFromRequestType() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setType("gradle-build");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getBuildSystem().id()).isEqualTo("gradle");
	}

	@Test
	void convertShouldSetDescriptionFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setDescription("This is my demo project");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getDescription()).isEqualTo("This is my demo project");
	}

	@Test
	void convertShouldSetPackagingFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setPackaging("war");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getPackaging().id()).isEqualTo("war");
	}

	@Test
	void convertShouldSetPlatformVersionFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setBootVersion("2.0.3");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getPlatformVersion()).isEqualTo(Version.parse("2.0.3"));
	}

	@Test
	void convertShouldUseDefaultPlatformVersionFromMetadata() {
		BasicProjectRequest request = getBasicProjectRequest();
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getPlatformVersion())
				.isEqualTo(Version.parse("2.1.1.RELEASE"));
	}

	@Test
	void convertShouldSetLanguageForProjectDescriptionFromRequest() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setJavaVersion("1.8");
		ProjectDescription description = this.converter.convert(request);
		assertThat(description.getLanguage().id()).isEqualTo("java");
		assertThat(description.getLanguage().jvmVersion()).isEqualTo("1.8");
	}

	@Test
	void convertWhenConversionFailsShouldThrowInvalidProjectRequestException() {
		BasicProjectRequest request = getBasicProjectRequest();
		request.setJavaVersion("1.8");
		request.setPackaging("star");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request))
				.withMessage("Unrecognized packaging id 'star'");
	}

	private BasicProjectRequest getBasicProjectRequest() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setLanguage("java");
		request.setPackaging("jar");
		request.setType("maven-build");
		return request;
	}

}
