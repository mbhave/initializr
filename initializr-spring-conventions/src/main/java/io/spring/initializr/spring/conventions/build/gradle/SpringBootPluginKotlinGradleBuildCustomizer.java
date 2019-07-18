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

package io.spring.initializr.spring.conventions.build.gradle;

import io.spring.initializr.conventions.build.BuildCustomizer;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * @author Madhura Bhave
 */
public class SpringBootPluginKotlinGradleBuildCustomizer implements BuildCustomizer<GradleBuild> {

	private final ResolvedProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public SpringBootPluginKotlinGradleBuildCustomizer(ResolvedProjectDescription projectDescription,
			InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Override
	public void customize(GradleBuild build) {
		build.addPlugin("org.springframework.boot", projectDescription.getPlatformVersion().toString());
		build.addPlugin("io.spring.dependency-management",
				metadata.getConfiguration().getEnv().getGradle().getDependencyManagementPluginVersion());
	}

}
