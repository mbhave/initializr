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

package io.spring.initializr.spring.conventions.configuration.build.infrastructure;

import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.conventions.build.BuildCustomizer;
import io.spring.initializr.conventions.build.maven.MavenBuildProjectContributor;
import io.spring.initializr.conventions.build.maven.MavenWrapperContributor;
import io.spring.initializr.conventions.util.LambdaSafe;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * Configuration for Maven build system infrastructure.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(MavenBuildSystem.ID)
public class MavenProjectGenerationConfiguration {

	@Bean
	public MavenWrapperContributor mavenWrapperContributor() {
		return new MavenWrapperContributor();
	}

	@Bean
	public MavenBuild mavenBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
			ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
		return createBuild(buildItemResolver.getIfAvailable(),
				buildCustomizers.orderedStream().collect(Collectors.toList()));
	}

	@SuppressWarnings("unchecked")
	private MavenBuild createBuild(BuildItemResolver buildItemResolver, List<BuildCustomizer<?>> buildCustomizers) {
		MavenBuild build = (buildItemResolver != null) ? new MavenBuild(buildItemResolver) : new MavenBuild();
		LambdaSafe.callbacks(BuildCustomizer.class, buildCustomizers, build)
				.invoke((customizer) -> customizer.customize(build));
		return build;
	}

	@Bean
	public MavenBuildProjectContributor mavenBuildProjectContributor(MavenBuild build,
			IndentingWriterFactory indentingWriterFactory) {
		return new MavenBuildProjectContributor(build, indentingWriterFactory);
	}

}
