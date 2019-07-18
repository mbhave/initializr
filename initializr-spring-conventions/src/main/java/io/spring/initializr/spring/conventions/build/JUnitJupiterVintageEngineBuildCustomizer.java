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

package io.spring.initializr.spring.conventions.build;

import io.spring.initializr.conventions.build.BuildCustomizer;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;

/**
 * @author Madhura Bhave
 */
public class JUnitJupiterVintageEngineBuildCustomizer implements BuildCustomizer {

	@Override
	public void customize(Build build) {
		build.dependencies().add("test",
				Dependency.withCoordinates("org.springframework.boot", "spring-boot-starter-test")
						.scope(DependencyScope.TEST_COMPILE)
						.exclusions(new Dependency.Exclusion("org.junit.vintage", "junit-vintage-engine"),
								new Dependency.Exclusion("junit", "junit")));
	}

}
