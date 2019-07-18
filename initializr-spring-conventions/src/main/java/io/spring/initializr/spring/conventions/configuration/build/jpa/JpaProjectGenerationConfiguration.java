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

package io.spring.initializr.spring.conventions.configuration.build.jpa;

import io.spring.initializr.conventions.code.kotlin.KotlinJpaGradleBuildCustomizer;
import io.spring.initializr.conventions.code.kotlin.KotlinJpaMavenBuildCustomizer;
import io.spring.initializr.conventions.code.kotlin.KotlinProjectSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.context.annotation.Bean;

/**
 * @author Madhura Bhave
 */
@ProjectGenerationConfiguration
public class JpaProjectGenerationConfiguration {

	@Bean
	@ConditionalOnLanguage(KotlinLanguage.ID)
	@ConditionalOnBuildSystem(GradleBuildSystem.ID)
	public KotlinJpaGradleBuildCustomizer kotlinJpaGradleBuildCustomizer(InitializrMetadata metadata,
			KotlinProjectSettings settings) {
		return new KotlinJpaGradleBuildCustomizer(metadata, settings);
	}

	@Bean
	@ConditionalOnLanguage(KotlinLanguage.ID)
	@ConditionalOnBuildSystem(MavenBuildSystem.ID)
	public KotlinJpaMavenBuildCustomizer kotlinJpaMavenBuildCustomizer(InitializrMetadata metadata) {
		return new KotlinJpaMavenBuildCustomizer(metadata);
	}

}
