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

package io.spring.initializr.spring.conventions.configuration.build.packaging;

import io.spring.initializr.conventions.build.maven.MavenWarPackagingBuildCustomizer;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnPackaging;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.spring.conventions.build.WarPackagingWebStarterBuildCustomizer;

import org.springframework.context.annotation.Bean;

/**
 * @author Madhura Bhave
 */
@ProjectGenerationConfiguration
@ConditionalOnPackaging(WarPackaging.ID)
public class WarPackagingProjectGenerationConfiguration {

	@Bean
	public WarPackagingWebStarterBuildCustomizer warPackagingWebStarterBuildCustomizer(InitializrMetadata metadata) {
		return new WarPackagingWebStarterBuildCustomizer(metadata);
	}

	@Bean
	@ConditionalOnBuildSystem(MavenBuildSystem.ID)
	public MavenWarPackagingBuildCustomizer mavenWarPackagingConfigurer() {
		return new MavenWarPackagingBuildCustomizer();
	}

}
