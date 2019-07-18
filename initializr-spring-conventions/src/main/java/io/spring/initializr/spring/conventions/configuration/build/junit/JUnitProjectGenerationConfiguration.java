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

package io.spring.initializr.spring.conventions.configuration.build.junit;

import io.spring.initializr.conventions.build.gradle.JUnitJupiterGradleBuildCustomizer;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnPlatformVersion;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.spring.conventions.build.JUnit4StarterBuildCustomizer;
import io.spring.initializr.spring.conventions.build.JUnitJupiterVintageEngineBuildCustomizer;

import org.springframework.context.annotation.Bean;

/**
 * {@link ProjectGenerationConfiguration} for JUnit.
 *
 * @author Madhura Bhave
 */
@ProjectGenerationConfiguration
public class JUnitProjectGenerationConfiguration {

	@Bean
	@ConditionalOnPlatformVersion("[1.5.0.RELEASE,2.2.0.M3)")
	public JUnit4StarterBuildCustomizer jUnit4StarterBuildCustomizer() {
		return new JUnit4StarterBuildCustomizer();
	}

	@Bean
	@ConditionalOnPlatformVersion("2.2.0.M3")
	public JUnitJupiterVintageEngineBuildCustomizer jUnitJupiterVintageEngineBuildCustomizer() {
		return new JUnitJupiterVintageEngineBuildCustomizer();
	}

	@Bean
	@ConditionalOnPlatformVersion("2.2.0.M3")
	@ConditionalOnBuildSystem(GradleBuildSystem.ID)
	public JUnitJupiterGradleBuildCustomizer jUnitJupiterGradleBuildCustomizer() {
		return new JUnitJupiterGradleBuildCustomizer();
	}

}
