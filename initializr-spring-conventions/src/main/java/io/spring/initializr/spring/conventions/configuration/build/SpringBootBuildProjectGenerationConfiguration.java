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

package io.spring.initializr.spring.conventions.configuration.build;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnPlatformVersion;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.spring.conventions.build.DefaultStarterBuildCustomizer;
import io.spring.initializr.spring.conventions.build.SpringBootVersionRepositoriesBuildCustomizer;
import io.spring.initializr.spring.conventions.build.gradle.ConditionalOnGradleVersion;
import io.spring.initializr.spring.conventions.build.gradle.DependencyManagementGradleBuildCustomizer;
import io.spring.initializr.spring.conventions.build.gradle.SpringBootPluginGradle3BuildCustomizer;
import io.spring.initializr.spring.conventions.build.gradle.SpringBootPluginGroovyGradleBuildCustomizer;
import io.spring.initializr.spring.conventions.build.gradle.SpringBootPluginKotlinGradleBuildCustomizer;
import io.spring.initializr.spring.conventions.build.maven.DefaultMavenBuildCustomizer;

import org.springframework.context.annotation.Bean;

/**
 * @author Madhura Bhave
 */
@ProjectGenerationConfiguration
public class SpringBootBuildProjectGenerationConfiguration {

	@Bean
	public DefaultStarterBuildCustomizer defaultStarterContributor(InitializrMetadata metadata) {
		return new DefaultStarterBuildCustomizer(metadata);
	}

	@Bean
	public SpringBootVersionRepositoriesBuildCustomizer repositoriesBuilderCustomizer(
			ResolvedProjectDescription description) {
		return new SpringBootVersionRepositoriesBuildCustomizer(description.getPlatformVersion());
	}

	@Bean
	@ConditionalOnBuildSystem(MavenBuildSystem.ID)
	public DefaultMavenBuildCustomizer initializrMetadataMavenBuildCustomizer(
			ResolvedProjectDescription projectDescription, InitializrMetadata metadata) {
		return new DefaultMavenBuildCustomizer(projectDescription, metadata);
	}

	@Bean
	@ConditionalOnGradleVersion("3")
	@ConditionalOnBuildSystem(GradleBuildSystem.ID)
	public SpringBootPluginGradle3BuildCustomizer gradle3BuildCustomizer(
			ResolvedProjectDescription projectDescription) {
		return new SpringBootPluginGradle3BuildCustomizer(projectDescription);
	}

	@Bean
	@ConditionalOnGradleVersion({ "4", "5" })
	@ConditionalOnBuildSystem(value = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_GROOVY)
	public SpringBootPluginGroovyGradleBuildCustomizer groovyGradleBuildCustomizer(
			ResolvedProjectDescription projectDescription) {
		return new SpringBootPluginGroovyGradleBuildCustomizer(projectDescription);
	}

	@Bean
	@ConditionalOnGradleVersion("5")
	@ConditionalOnBuildSystem(value = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_KOTLIN)
	public SpringBootPluginKotlinGradleBuildCustomizer kotlinGradleBuildCustomizer(
			ResolvedProjectDescription projectDescription, InitializrMetadata metadata) {
		return new SpringBootPluginKotlinGradleBuildCustomizer(projectDescription, metadata);
	}

	@Bean
	@ConditionalOnPlatformVersion("2.0.0.M1")
	@ConditionalOnBuildSystem(id = GradleBuildSystem.ID, dialect = GradleBuildSystem.DIALECT_GROOVY)
	public DependencyManagementGradleBuildCustomizer dependencyManagementGradleBuildCustomizer() {
		return new DependencyManagementGradleBuildCustomizer();
	}

}
