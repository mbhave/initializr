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

import java.util.Collections;

import io.spring.initializr.generator.BasicProjectRequest;
import io.spring.initializr.generator.InvalidProjectRequestException;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link InitializrMetdataProjectRequestValidator}.
 *
 * @author Madhura Bhave
 */
public class InitializrMetdataProjectRequestValidatorTests {

	private InitializrMetdataProjectRequestValidator validator;

	@BeforeEach
	void setup() {
		InitializrMetadataProvider provider = mock(InitializrMetadataProvider.class);
		given(provider.get())
				.willReturn(InitializrMetadataTestBuilder.withDefaults().build());
		this.validator = new InitializrMetdataProjectRequestValidator(provider);
	}

	@Test
	public void validateWhenTypeIsInvalidShouldThrowException() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setType("foo-build");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.validator.validate(request))
				.withMessage("Unknown type 'foo-build' check project metadata");
	}

	@Test
	public void validateWhenPackagingIsInvalidShouldThrowException() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setPackaging("star");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.validator.validate(request))
				.withMessage("Unknown packaging 'star' check project metadata");
	}

	@Test
	public void validateWhenLanguageIsInvalidShouldThrowException() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setLanguage("english");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.validator.validate(request))
				.withMessage("Unknown language 'english' check project metadata");
	}

	@Test
	void validateWhenDependencyNotPresentShouldThrowException() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setDependencies(Collections.singletonList("invalid"));
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.validator.validate(request))
				.withMessage("Unknown dependency 'invalid' check project metadata");
	}

	@Test
	void validateWhenProjectRequestValidShouldNotThrowException() {
		BasicProjectRequest request = new BasicProjectRequest();
		request.setType("maven-project");
		request.setPackaging("jar");
		request.setLanguage("java");
		this.validator.validate(request);
	}

}
