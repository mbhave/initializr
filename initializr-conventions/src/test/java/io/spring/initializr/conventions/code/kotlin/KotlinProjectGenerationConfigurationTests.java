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

package io.spring.initializr.conventions.code.kotlin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.conventions.build.BuildProjectGenerationConfiguration;
import io.spring.initializr.conventions.build.maven.MavenProjectGenerationConfiguration;
import io.spring.initializr.conventions.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.conventions.test.InitializrMetadataTestBuilder;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.InitializrMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KotlinProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
class KotlinProjectGenerationConfigurationTests {

	private ProjectAssetTester projectTester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.projectTester = new ProjectAssetTester().withIndentingWriterFactory()
				.withConfiguration(SourceCodeProjectGenerationConfiguration.class,
						KotlinProjectGenerationConfiguration.class, BuildProjectGenerationConfiguration.class,
						MavenProjectGenerationConfiguration.class)
				.withDirectory(directory)
				.withBean(KotlinProjectSettings.class, () -> new SimpleKotlinProjectSettings("1.2.70"))
				.withBean(InitializrMetadata.class, () -> {
					io.spring.initializr.metadata.Dependency dependency = io.spring.initializr.metadata.Dependency
							.withId("foo");
					dependency.setFacets(Collections.singletonList("json"));
					return InitializrMetadataTestBuilder.withDefaults().addDependencyGroup("test", dependency).build();
				}).withDescriptionCustomizer((description) -> {
					description.setLanguage(new KotlinLanguage());
					if (description.getPlatformVersion() == null) {
						description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
					}
					description.setBuildSystem(new MavenBuildSystem());
				});
	}

	@Test
	void mainClassIsContributedWhenGeneratingProject() {
		ProjectStructure projectStructure = this.projectTester.generate(new ProjectDescription());
		assertThat(projectStructure.getRelativePathsOfProjectFiles())
				.contains("src/main/kotlin/com/example/demo/DemoApplication.kt");
	}

	@Test
	void testClassIsContributedWithJunit4() {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.4.RELEASE"));
		ProjectStructure projectStructure = this.projectTester.generate(description);
		assertThat(projectStructure.getRelativePathsOfProjectFiles())
				.contains("src/test/kotlin/com/example/demo/DemoApplicationTests.kt");
		List<String> lines = projectStructure.readAllLines("src/test/kotlin/com/example/demo/DemoApplicationTests.kt");
		assertThat(lines).containsExactly("package com.example.demo", "", "import org.junit.Test",
				"import org.junit.runner.RunWith", "import org.springframework.boot.test.context.SpringBootTest",
				"import org.springframework.test.context.junit4.SpringRunner", "", "@RunWith(SpringRunner::class)",
				"@SpringBootTest", "class DemoApplicationTests {", "", "    @Test", "    fun contextLoads() {", "    }",
				"", "}");
	}

	@Test
	void testClassIsContributedWithJunit5() {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.2.0.RELEASE"));
		ProjectStructure projectStructure = this.projectTester.generate(description);
		assertThat(projectStructure.getRelativePathsOfProjectFiles())
				.contains("src/test/kotlin/com/example/demo/DemoApplicationTests.kt");
		List<String> lines = projectStructure.readAllLines("src/test/kotlin/com/example/demo/DemoApplicationTests.kt");
		assertThat(lines).containsExactly("package com.example.demo", "", "import org.junit.jupiter.api.Test",
				"import org.springframework.boot.test.context.SpringBootTest", "", "@SpringBootTest",
				"class DemoApplicationTests {", "", "    @Test", "    fun contextLoads() {", "    }", "", "}");
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging() {
		ProjectDescription description = new ProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("KotlinDemoApplication");
		ProjectStructure projectStructure = this.projectTester.generate(description);
		assertThat(projectStructure.getRelativePathsOfProjectFiles())
				.contains("src/main/kotlin/com/example/demo/ServletInitializer.kt");
		List<String> lines = projectStructure.readAllLines("src/main/kotlin/com/example/demo/ServletInitializer.kt");
		assertThat(lines).containsExactly("package com.example.demo", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer", "",
				"class ServletInitializer : SpringBootServletInitializer() {", "",
				"    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {",
				"        return application.sources(KotlinDemoApplication::class.java)", "    }", "", "}");
	}

	@Test
	void jacksonKotlinModuleShouldBeAddedWhenJsonFacetPresent() {
		ProjectDescription description = new ProjectDescription();
		description.addDependency("foo", Dependency.withCoordinates("com.example", "foo").build());
		ProjectStructure projectStructure = this.projectTester.generate(description);
		List<String> lines = projectStructure.readAllLines("pom.xml");
		assertThat(lines).contains("        <dependency>",
				"            <groupId>com.fasterxml.jackson.module</groupId>",
				"            <artifactId>jackson-module-kotlin</artifactId>", "        </dependency>");
	}

}
