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

import java.util.List;

import io.spring.initializr.generator.BasicProjectRequest;
import io.spring.initializr.generator.InvalidProjectRequestException;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.Type;

/**
 * A {@link ProjectRequestValidator} that uses the metadata to validate the project
 * request.
 *
 * @author Madhura Bhave
 */
public class InitializrMetdataProjectRequestValidator implements ProjectRequestValidator {

	private final InitializrMetadataProvider metadataProvider;

	public InitializrMetdataProjectRequestValidator(
			InitializrMetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider;
	}

	@Override
	public void validate(BasicProjectRequest request) {
		InitializrMetadata metadata = this.metadataProvider.get();
		validateType(request.getType(), metadata);
		validateLanguage(request.getLanguage(), metadata);
		validatePackaging(request.getPackaging(), metadata);
		validateDependencies(request, metadata);
	}

	private void validateType(String type, InitializrMetadata metadata) {
		if (type != null) {
			Type typeFromMetadata = metadata.getTypes().get(type);
			if (typeFromMetadata == null) {
				throw new InvalidProjectRequestException(
						"Unknown type '" + type + "' check project metadata");
			}
		}
	}

	private void validateLanguage(String language, InitializrMetadata metadata) {
		if (language != null) {
			DefaultMetadataElement languageFromMetadata = metadata.getLanguages()
					.get(language);
			if (languageFromMetadata == null) {
				throw new InvalidProjectRequestException(
						"Unknown language '" + language + "' check project metadata");
			}
		}
	}

	private void validatePackaging(String packaging, InitializrMetadata metadata) {
		if (packaging != null) {
			DefaultMetadataElement packagingFromMetadata = metadata.getPackagings()
					.get(packaging);
			if (packagingFromMetadata == null) {
				throw new InvalidProjectRequestException(
						"Unknown packaging '" + packaging + "' check project metadata");
			}
		}
	}

	private void validateDependencies(BasicProjectRequest request,
			InitializrMetadata metadata) {
		List<String> dependencies = (!request.getStyle().isEmpty() ? request.getStyle()
				: request.getDependencies());
		dependencies.forEach((dep) -> {
			Dependency dependency = metadata.getDependencies().get(dep);
			if (dependency == null) {
				throw new InvalidProjectRequestException(
						"Unknown dependency '" + dep + "' check project metadata");
			}
		});
	}

}
