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

package io.spring.initializr.generator;

import java.util.Collections;

import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.test.generator.ProjectAssert;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import io.spring.initializr.util.VersionProperty;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link ProjectGenerator}
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 */
public class ProjectGeneratorTests extends AbstractProjectGeneratorTests {

	@Override
	protected InitializrMetadataTestBuilder initializeTestMetadataBuilder() {
		return InitializrMetadataTestBuilder.withBasicDefaults();
	}

	@Test
	public void defaultMavenPom() {
		ProjectRequest request = createProjectRequest("web");
		generateMavenPom(request).hasNoRepository().hasSpringBootStarterDependency("web");
		verifyProjectSuccessfulEventFor(request);
	}

	@Test
	public void defaultGradleBuild() {
		ProjectRequest request = createProjectRequest("web");
		generateGradleBuild(request).doesNotContain("import");
		verifyProjectSuccessfulEventFor(request);
	}

	@Test
	public void defaultProject() {
		ProjectRequest request = createProjectRequest("web");
		generateProject(request).isJavaProject().isMavenProject().pomAssert()
				.hasNoRepository().hasSpringBootStarterDependency("web");
		verifyProjectSuccessfulEventFor(request);
	}

	@Test
	public void defaultProjectWithGradle() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-build");
		ProjectAssert gradleProject = generateProject(request).isGradleProject();
		gradleProject.gradleBuildAssert().contains(
				"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"testImplementation 'org.springframework.boot:spring-boot-starter-test'");
		gradleProject.gradleSettingsAssert().hasProjectName("demo");
		verifyProjectSuccessfulEventFor(request);
	}

	@Test
	public void noDependencyAddsRootStarter() {
		ProjectRequest request = createProjectRequest();
		generateProject(request).isJavaProject().isMavenProject().pomAssert()
				.hasSpringBootStarterRootDependency();
	}

	@Test
	public void mavenPomWithBootSnapshot() {
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("2.1.1.BUILD-SNAPSHOT");
		generateMavenPom(request).hasSnapshotRepository()
				.hasSpringBootParent("2.1.1.BUILD-SNAPSHOT")
				.hasSpringBootStarterDependency("web");
	}

	@Test
	public void mavenPomWithTarDependency() {
		Dependency dependency = Dependency.withId("custom-artifact", "org.foo",
				"custom-artifact");
		dependency.setType("tar.gz");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("test", dependency).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("custom-artifact");
		generateMavenPom(request).hasDependency(dependency).hasDependenciesCount(2);
	}

	@Test
	public void gradleBuildWithTarDependency() {
		Dependency dependency = Dependency.withId("custom-artifact", "org.foo",
				"custom-artifact");
		dependency.setType("tar.gz");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("test", dependency).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("custom-artifact");
		generateGradleBuild(request)
				.contains("implementation 'org.foo:custom-artifact@tar.gz'");
	}

	@Test
	public void mavenPomWithWebFacet() {
		Dependency dependency = Dependency.withId("thymeleaf", "org.foo", "thymeleaf");
		dependency.getFacets().add("web");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("test", dependency).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("thymeleaf");
		generateMavenPom(request).hasDependency("org.foo", "thymeleaf")
				.hasDependenciesCount(2);
	}

	@Test
	public void mavenWarWithWebFacet() {
		Dependency dependency = Dependency.withId("thymeleaf", "org.foo", "thymeleaf");
		dependency.getFacets().add("web");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("test", dependency).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("thymeleaf");
		request.setPackaging("war");
		generateProject(request).isJavaWarProject().isMavenProject().pomAssert()
				.hasSpringBootStarterTomcat()
				// This is tagged as web facet so it brings the web one
				.hasDependency("org.foo", "thymeleaf").hasSpringBootStarterTest()
				.hasDependenciesCount(3);
	}

	@Test
	public void mavenWarPomWithoutWebFacet() {
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setPackaging("war");
		generateMavenPom(request).hasSpringBootStarterTomcat()
				.hasSpringBootStarterDependency("data-jpa")
				.hasSpringBootStarterDependency("web") // Added by war packaging
				.hasSpringBootStarterTest().hasDependenciesCount(4);
	}

	@Test
	public void mavenWarPomWithoutWebFacetAndWithoutWebDependency() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "security", "data-jpa").build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("data-jpa");
		request.setPackaging("war");
		generateMavenPom(request).hasSpringBootStarterTomcat()
				.hasSpringBootStarterDependency("data-jpa")
				.hasSpringBootStarterDependency("web") // Added by war packaging
				.hasSpringBootStarterTest().hasDependenciesCount(4);
	}

	@Test
	public void mavenWarPomWithoutWebFacetAndWithCustomWebDependency() {
		Dependency customWebStarter = Dependency.withId("web", "org.acme", "web-starter");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "security", "data-jpa")
				.addDependencyGroup("acme", customWebStarter).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setPackaging("war");
		generateMavenPom(request).hasSpringBootStarterTomcat()
				.hasSpringBootStarterDependency("data-jpa")
				.hasDependency(customWebStarter) // Added by war packaging
				.hasSpringBootStarterTest().hasDependenciesCount(4);
	}

	@Test
	public void gradleWarWithWebFacet() {
		Dependency dependency = Dependency.withId("thymeleaf", "org.foo", "thymeleaf");
		dependency.getFacets().add("web");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("test", dependency).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("thymeleaf");
		request.setPackaging("war");
		request.setType("gradle-project");
		generateProject(request).isJavaWarProject().isGradleProject().gradleBuildAssert()
				// This is tagged as web facet so it brings the web one
				.contains("plugins {").contains("id 'war'")
				.contains("implementation 'org.foo:thymeleaf'")
				.doesNotContain(
						"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"testImplementation 'org.springframework.boot:spring-boot-starter-test'")
				.doesNotContain("configurations {") // no need to declare providedRuntime
				.contains("providedRuntime").contains(
						"providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'");
	}

	@Test
	public void gradleWarPomWithoutWebFacet() {
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setPackaging("war");
		generateGradleBuild(request).contains(
				"implementation 'org.springframework.boot:spring-boot-starter-data-jpa'")
				// Added by warpackaging
				.contains(
						"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"testImplementation 'org.springframework.boot:spring-boot-starter-test'")
				.doesNotContain("configurations {") // no need to declare providedRuntime
				.contains("providedRuntime").contains(
						"providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'");
	}

	@Test
	public void groupIdAndArtifactIdInferPackageName() {
		ProjectRequest request = createProjectRequest("web");
		request.setGroupId("org.acme");
		request.setArtifactId("42foo");
		generateProject(request).isJavaProject("org/acme/foo", "DemoApplication");
	}

	@Test
	public void cleanPackageNameWithGroupIdAndArtifactIdWithVersion() {
		ProjectRequest request = createProjectRequest("web");
		request.setGroupId("org.acme");
		request.setArtifactId("foo-1.4.5");
		assertProjectWithPackageNameWithVersion(request);
	}

	@Test
	public void cleanPackageNameWithInvalidPackageName() {
		ProjectRequest request = createProjectRequest("web");
		request.setGroupId("org.acme");
		request.setArtifactId("foo");
		request.setPackageName("org.acme.foo-1.4.5");
		assertProjectWithPackageNameWithVersion(request);
	}

	private void assertProjectWithPackageNameWithVersion(ProjectRequest request) {
		generateProject(request).isJavaProject("org/acme/foo145", "DemoApplication")
				.sourceCodeAssert("src/main/java/org/acme/foo145/DemoApplication.java")
				.contains("package org.acme.foo145;");
	}

	@Test
	public void gradleProjectWithCustomArtifactId() {
		ProjectRequest request = createProjectRequest();
		request.setType("gradle-build");
		request.setArtifactId("my-application");
		generateProject(request).isGradleProject().gradleSettingsAssert()
				.hasProjectName("my-application");
		verifyProjectSuccessfulEventFor(request);
	}

	@Test
	public void springBootUseSpringBootApplicationJava() {
		ProjectRequest request = createProjectRequest("web");
		request.setName("MyDemo");
		request.setPackageName("foo");
		generateProject(request)
				.sourceCodeAssert("src/main/java/foo/MyDemoApplication.java")
				.hasImports(SpringBootApplication.class.getName())
				.contains("@SpringBootApplication");
	}

	@Test
	public void springBootUseSpringBootApplicationGroovy() {
		ProjectRequest request = createProjectRequest("web");
		request.setLanguage("groovy");
		request.setName("MyDemo");
		request.setPackageName("foo");
		generateProject(request)
				.sourceCodeAssert("src/main/groovy/foo/MyDemoApplication.groovy")
				.hasImports(SpringBootApplication.class.getName())
				.contains("@SpringBootApplication");
	}

	@Test
	public void springBootUseSpringBootApplicationKotlin() {
		ProjectRequest request = createProjectRequest("web");
		request.setLanguage("kotlin");
		request.setName("MyDemo");
		request.setPackageName("foo");

		applyMetadata(initializeTestMetadataBuilder().addDependencyGroup("core", "web")
				.setKotlinEnv("1.0.0").build());
		generateProject(request)
				.sourceCodeAssert("src/main/kotlin/foo/MyDemoApplication.kt")
				.hasImports(SpringBootApplication.class.getName())
				.contains("@SpringBootApplication");
	}

	@Test
	public void springBoot15UseGradle3() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-project");
		request.setBootVersion("1.5.0.RELEASE");
		generateProject(request).isGradleProject("3.5.1");
	}

	@Test
	public void springBoot20UsesGradle4() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-project");
		request.setBootVersion("2.0.0.RELEASE");
		generateProject(request).isGradleProject("4.10.2");
	}

	@Test
	public void customBaseDirectory() {
		ProjectRequest request = createProjectRequest();
		request.setBaseDir("my-project");
		generateProject(request).hasBaseDir("my-project").isJavaProject()
				.isMavenProject();
	}

	@Test
	public void customBaseDirectoryNested() {
		ProjectRequest request = createProjectRequest();
		request.setBaseDir("foo-bar/my-project");
		generateProject(request).hasBaseDir("foo-bar/my-project").isJavaProject()
				.isMavenProject();
	}

	@Test
	public void groovyWithMavenUsesGroovyDir() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("maven-project");
		request.setLanguage("groovy");
		generateProject(request).isMavenProject().isGroovyProject();
	}

	@Test
	public void groovyWithGradleUsesGroovyDir() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-project");
		request.setLanguage("groovy");
		generateProject(request).isGradleProject().isGroovyProject();
	}

	@Test
	public void mavenPomWithCustomVersion() {
		Dependency whatever = Dependency.withId("whatever", "org.acme", "whatever",
				"1.2.3");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("foo", whatever).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("whatever", "data-jpa", "web");
		generateMavenPom(request).hasDependency(whatever)
				.hasSpringBootStarterDependency("data-jpa")
				.hasSpringBootStarterDependency("web");
	}

	@Test
	public void defaultMavenPomHasSpringBootParent() {
		ProjectRequest request = createProjectRequest("web");
		generateMavenPom(request).hasSpringBootParent(request.getBootVersion())
				.hasNoProperty("project.build.sourceEncoding")
				.hasNoProperty("project.reporting.outputEncoding");
	}

	@Test
	public void mavenPomWithCustomParentPom() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.setMavenParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT", false).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("web");
		generateMavenPom(request).hasParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT")
				.hasBomsCount(0).hasProperty("project.build.sourceEncoding", "UTF-8")
				.hasProperty("project.reporting.outputEncoding", "UTF-8");
	}

	@Test
	public void mavenPomWithCustomParentPomAndSpringBootBom() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.setMavenParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT", true).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("1.5.17.RELEASE");
		generateMavenPom(request).hasParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT")
				.hasProperty("spring-boot.version", "1.5.17.RELEASE")
				.hasBom("org.springframework.boot", "spring-boot-dependencies",
						"${spring-boot.version}")
				.hasBomsCount(1).hasProperty("project.build.sourceEncoding", "UTF-8")
				.hasProperty("project.reporting.outputEncoding", "UTF-8");
	}

	@Test
	public void gradleBuildWithCustomParentPomAndSpringBootBom() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.setMavenParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT", true).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("1.5.17.RELEASE");
		generateGradleBuild(request)
				.doesNotContain("ext['spring-boot.version'] = '1.5.17.RELEASE'")
				.doesNotContain(
						"mavenBom \"org.springframework.boot:spring-boot-dependencies:1.5.17.RELEASE\"");
	}

	@Test
	public void gradleBuildWithBootSnapshot() {
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("2.1.1.BUILD-SNAPSHOT");
		generateGradleBuild(request).hasSnapshotRepository();
	}

	@Test
	public void gradleBuildWithCustomVersion() {
		Dependency whatever = Dependency.withId("whatever", "org.acme", "whatever",
				"1.2.3");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("foo", whatever).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("whatever", "data-jpa", "web");
		generateGradleBuild(request).contains(
				"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"implementation 'org.springframework.boot:spring-boot-starter-data-jpa'")
				.contains("implementation 'org.acme:whatever:1.2.3'");
	}

	@Test
	public void mavenPomWithCustomScope() {
		Dependency h2 = Dependency.withId("h2", "org.h2", "h2");
		h2.setScope("runtime");
		Dependency hamcrest = Dependency.withId("hamcrest", "org.hamcrest", "hamcrest");
		hamcrest.setScope("test");
		Dependency servlet = Dependency.withId("servlet-api", "javax.servlet",
				"servlet-api");
		servlet.setScope("provided");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("database", h2)
				.addDependencyGroup("container", servlet)
				.addDependencyGroup("test", hamcrest).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("hamcrest", "h2", "servlet-api",
				"data-jpa", "web");
		generateMavenPom(request).hasDependency(h2).hasDependency(hamcrest)
				.hasDependency(servlet).hasSpringBootStarterDependency("data-jpa")
				.hasSpringBootStarterDependency("web");
	}

	@Test
	public void gradleBuildWithCustomScope() {
		Dependency h2 = Dependency.withId("h2", "org.h2", "h2");
		h2.setScope("runtime");
		Dependency hamcrest = Dependency.withId("hamcrest", "org.hamcrest", "hamcrest");
		hamcrest.setScope("test");
		Dependency servlet = Dependency.withId("servlet-api", "javax.servlet",
				"servlet-api");
		servlet.setScope("provided");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("database", h2)
				.addDependencyGroup("container", servlet)
				.addDependencyGroup("test", hamcrest).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("hamcrest", "h2", "servlet-api",
				"data-jpa", "web");
		generateGradleBuild(request).contains(
				"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"implementation 'org.springframework.boot:spring-boot-starter-data-jpa'")
				// declare providedRuntime config for jar-based projects
				.contains("runtimeOnly 'org.h2:h2'").contains("configurations {")
				.contains("providedRuntime")
				.contains("providedRuntime 'javax.servlet:servlet-api'")
				.contains("testImplementation 'org.hamcrest:hamcrest'");
	}

	@Test
	public void gradleBuildWithSpringBoot15() {
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("1.5.20.BUILD-SNAPSHOT");
		generateGradleBuild(request).contains("apply plugin: 'org.springframework.boot'")
				.contains(
						"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"testImplementation 'org.springframework.boot:spring-boot-starter-test'")
				.doesNotContain("apply plugin: 'spring-boot'");
	}

	@Test
	public void gradleBuildWithSpringBoot20() {
		ProjectRequest request = createProjectRequest("web");
		request.setBootVersion("2.0.0.RELEASE");
		generateGradleBuild(request).contains("plugins {")
				.contains("id 'org.springframework.boot' version '2.0.0.RELEASE'")
				.doesNotContain("apply plugin: 'spring-boot'")
				.contains("apply plugin: 'io.spring.dependency-management'")
				.contains(
						"implementation 'org.springframework.boot:spring-boot-starter-web'")
				.contains(
						"testImplementation 'org.springframework.boot:spring-boot-starter-test'");
	}

	@Test
	public void mavenBom() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setBom("foo-bom");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo)
				.addBom("foo-bom", "org.acme", "foo-bom", "1.2.3").build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo");
		generateMavenPom(request).hasDependency(foo).hasBom("org.acme", "foo-bom",
				"1.2.3");
	}

	@Test
	public void mavenBomWithSeveralDependenciesOnSameBom() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setBom("the-bom");
		Dependency bar = Dependency.withId("bar", "org.acme", "bar");
		bar.setBom("the-bom");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("group", foo, bar)
				.addBom("the-bom", "org.acme", "the-bom", "1.2.3").build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo", "bar");
		generateMavenPom(request).hasDependency(foo)
				.hasBom("org.acme", "the-bom", "1.2.3").hasBomsCount(1);
	}

	@Test
	public void mavenBomWithVersionMapping() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setBom("the-bom");
		BillOfMaterials bom = BillOfMaterials.create("org.acme", "foo-bom");
		bom.getMappings()
				.add(BillOfMaterials.Mapping.create("[2.2.0.RELEASE,2.3.0.M1)", "1.0.0"));
		bom.getMappings().add(BillOfMaterials.Mapping.create("2.3.0.M1", "1.2.0"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo).addBom("the-bom", bom).build();
		applyMetadata(metadata);

		// First version
		ProjectRequest request = createProjectRequest("foo");
		request.setBootVersion("2.2.5.RELEASE");
		generateMavenPom(request).hasDependency(foo).hasSpringBootParent("2.2.5.RELEASE")
				.hasBom("org.acme", "foo-bom", "1.0.0");

		// Second version
		ProjectRequest request2 = createProjectRequest("foo");
		request2.setBootVersion("2.3.0.M1");
		generateMavenPom(request2).hasDependency(foo).hasSpringBootParent("2.3.0.M1")
				.hasBom("org.acme", "foo-bom", "1.2.0");
	}

	@Test
	public void mavenBomWithVersionMappingAndExtraRepositories() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setBom("the-bom");
		BillOfMaterials bom = BillOfMaterials.create("org.acme", "foo-bom");
		bom.getRepositories().add("foo-repo");
		bom.getMappings()
				.add(BillOfMaterials.Mapping.create("[2.2.0.RELEASE,2.3.0.M1)", "1.0.0"));
		bom.getMappings().add(BillOfMaterials.Mapping.create("2.3.0.M1", "1.2.0",
				"foo-repo", "bar-repo"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo).addBom("the-bom", bom)
				.addRepository("foo-repo", "repo", "http://example.com/foo", true)
				.addRepository("bar-repo", "repo", "http://example.com/bar", false)
				.build();
		applyMetadata(metadata);

		// Second version
		ProjectRequest request = createProjectRequest("foo");
		request.setBootVersion("2.3.0.RELEASE");
		generateMavenPom(request).hasDependency(foo).hasSpringBootParent("2.3.0.RELEASE")
				.hasBom("org.acme", "foo-bom", "1.2.0")
				.hasRepository("foo-repo", "repo", "http://example.com/foo", true)
				.hasRepository("bar-repo", "repo", "http://example.com/bar", false)
				.hasRepositoriesCount(2);
	}

	@Test
	public void gradleBom() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setBom("foo-bom");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo)
				.addBom("foo-bom", "org.acme", "foo-bom", "1.2.3").build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo");
		generateGradleBuild(request).contains("dependencyManagement {")
				.contains("imports {").contains("mavenBom 'org.acme:foo-bom:1.2.3'");
	}

	@Test
	public void mavenRepository() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setRepository("foo-repo");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo)
				.addRepository("foo-repo", "foo", "http://example.com/repo", false)
				.build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo");
		generateMavenPom(request).hasDependency(foo).hasRepository("foo-repo", "foo",
				"http://example.com/repo", false);
	}

	@Test
	public void mavenRepositoryWithSeveralDependenciesOnSameRepository() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setRepository("the-repo");
		Dependency bar = Dependency.withId("bar", "org.acme", "bar");
		foo.setRepository("the-repo");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("group", foo, bar)
				.addRepository("the-repo", "repo", "http://example.com/repo", true)
				.build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo", "bar");
		generateMavenPom(request).hasDependency(foo)
				.hasRepository("the-repo", "repo", "http://example.com/repo", true)
				.hasRepositoriesCount(1);
	}

	@Test
	public void gradleRepository() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.setRepository("foo-repo");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo)
				.addRepository("foo-repo", "foo", "http://example.com/repo", false)
				.build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("foo");
		generateGradleBuild(request).hasRepository("http://example.com/repo");
	}

	@Test
	public void projectWithOnlyStarterDependency() {
		Dependency foo = Dependency.withId("foo", "org.foo", "custom-my-starter");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("foo");
		generateMavenPom(request).hasDependency("org.foo", "custom-my-starter")
				.hasSpringBootStarterTest().hasDependenciesCount(2);
	}

	@Test
	public void projectWithOnlyNonStarterDependency() {
		Dependency foo = Dependency.withId("foo", "org.foo", "foo");
		foo.setStarter(false);
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo).build();
		applyMetadata(metadata);

		ProjectRequest request = createProjectRequest("foo");
		generateMavenPom(request).hasDependency("org.foo", "foo")
				.hasSpringBootStarterRootDependency().hasSpringBootStarterTest()
				.hasDependenciesCount(3);
	}

	@Test
	public void buildPropertiesMaven() {
		ProjectRequest request = createProjectRequest("web");
		request.getBuildProperties().getMaven().put("name", () -> "test");
		request.getBuildProperties().getVersions().put(VersionProperty.of("foo.version"),
				() -> "1.2.3");
		request.getBuildProperties().getGradle().put("ignore.property", () -> "yes");

		generateMavenPom(request).hasProperty("name", "test")
				.hasProperty("foo.version", "1.2.3").hasNoProperty("ignore.property");
	}

	@Test
	public void buildPropertiesGradle() {
		ProjectRequest request = createProjectRequest("web");
		request.getBuildProperties().getGradle().put("name", () -> "test");
		request.getBuildProperties().getVersions()
				.put(VersionProperty.of("foo.version", false), () -> "1.2.3");
		request.getBuildProperties().getVersions()
				.put(VersionProperty.of("internal.version"), () -> "4.5.6");
		request.getBuildProperties().getMaven().put("ignore.property", () -> "yes");
		generateGradleBuild(request).hasProperties("name", "test", "foo.version", "1.2.3",
				"internalVersion", "4.5.6").doesNotContain("ignore.property");
	}

	@Test
	public void versionRangeWithPostProcessor() {
		Dependency foo = Dependency.withId("foo", "org.acme", "foo");
		foo.getMappings().add(Dependency.Mapping.create("[2.2.0.RELEASE,2.3.0.M1)", null,
				null, "1.0.0"));
		foo.getMappings().add(Dependency.Mapping.create("2.3.0.M1", null, null, "1.2.0"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("foo", foo).build();
		applyMetadata(metadata);

		// First without processor, get the correct version
		ProjectRequest request = createProjectRequest("foo");
		request.setBootVersion("2.2.5.RELEASE");
		generateMavenPom(request)
				.hasDependency(Dependency.withId("foo", "org.acme", "foo", "1.0.0"));

		// First after processor that flips Spring Boot version
		this.projectGenerator.setRequestResolver(new ProjectRequestResolver(
				Collections.singletonList(new ProjectRequestPostProcessor() {
					@Override
					public void postProcessBeforeResolution(ProjectRequest r,
							InitializrMetadata m) {
						r.setBootVersion("2.3.0.M2");
					}
				})));
		generateMavenPom(request)
				.hasDependency(Dependency.withId("foo", "org.acme", "foo", "1.2.0"));
	}

	@Test
	public void gitIgnoreMaven() {
		ProjectRequest request = createProjectRequest();
		request.setType("maven-project");
		ProjectAssert project = generateProject(request);
		project.sourceCodeAssert(".gitignore")
				.equalsTo(new ClassPathResource("project/maven/gitignore.gen"));
	}

	@Test
	public void gitIgnoreGradle() {
		ProjectRequest request = createProjectRequest();
		request.setType("gradle-project");
		ProjectAssert project = generateProject(request);
		project.sourceCodeAssert(".gitignore")
				.equalsTo(new ClassPathResource("project/gradle/gitignore.gen"));
	}

	@Test
	public void dependencyOrderSpringBootTakesPrecedence() {
		Dependency depOne = Dependency.withId("one", "org.acme", "first", "1.2.3");
		Dependency depTwo = Dependency.withId("two", "com.example", "second", "1.2.3");
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("core", "web", "security", "data-jpa")
				.addDependencyGroup("sample", depOne, depTwo).build();
		applyMetadata(metadata);
		ProjectRequest request = createProjectRequest("one", "web", "two", "data-jpa");
		assertThat(generateGradleBuild(request).getGradleBuild()).containsSubsequence(
				"implementation 'org.springframework.boot:spring-boot-starter-data-jpa'",
				"implementation 'org.springframework.boot:spring-boot-starter-web'",
				"implementation 'com.example:second:1.2.3'",
				"implementation 'org.acme:first:1.2.3'");
	}

	@Test
	public void invalidProjectTypeMavenPom() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-build");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.projectGenerator.generateMavenPom(request))
				.withMessageContaining("gradle-build");
	}

	@Test
	public void invalidProjectTypeGradleBuild() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("maven-build");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.projectGenerator.generateGradleBuild(request))
				.withMessageContaining("maven-build");
	}

	@Test
	public void invalidDependency() {
		ProjectRequest request = createProjectRequest("foo-bar");
		try {
			generateMavenPom(request);
			fail("Should have failed to generate project");
		}
		catch (InvalidProjectRequestException ex) {
			assertThat(ex.getMessage()).contains("foo-bar");
			verifyProjectFailedEventFor(request, ex);
		}
	}

	@Test
	public void invalidType() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("foo-bar");
		try {
			generateProject(request);
			fail("Should have failed to generate project");
		}
		catch (InvalidProjectRequestException ex) {
			assertThat(ex.getMessage()).contains("foo-bar");
			verifyProjectFailedEventFor(request, ex);
		}
	}

	@Test
	public void invalidPackaging() {
		ProjectRequest request = createProjectRequest("web");
		request.setPackaging("foo-bar");
		try {
			generateGradleBuild(request);
			fail("Should have failed to generate project");
		}
		catch (InvalidProjectRequestException ex) {
			assertThat(ex.getMessage()).contains("foo-bar");
			verifyProjectFailedEventFor(request, ex);
		}
	}

	@Test
	public void invalidLanguage() {
		ProjectRequest request = createProjectRequest("web");
		request.setLanguage("foo-bar");
		try {
			generateProject(request);
			fail("Should have failed to generate project");
		}
		catch (InvalidProjectRequestException ex) {
			assertThat(ex.getMessage()).contains("foo-bar");
			verifyProjectFailedEventFor(request, ex);
		}
	}

	@Test
	public void invalidSpringBootVersion() {
		ProjectRequest request = createProjectRequest("web");
		request.setType("maven-project");
		request.setBootVersion("1.2.3.M4");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.projectGenerator.generateMavenPom(request))
				.withMessageContaining("1.2.3.M4");
	}

	@Test
	public void kotlinWithMavenUseJpaFacetHasJpaKotlinPlugin() {
		applyJpaMetadata(true);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("maven-project");
		request.setLanguage("kotlin");
		generateMavenPom(request).contains("<plugin>jpa</plugin>")
				.contains("kotlin-maven-noarg");
	}

	@Test
	public void kotlinWithMavenWithoutJpaFacetDoesNotHaveJpaKotlinPlugin() {
		applyJpaMetadata(false);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("maven-project");
		request.setLanguage("kotlin");
		generateMavenPom(request).doesNotContain("<plugin>jpa</plugin>")
				.doesNotContain("kotlin-maven-noarg");
	}

	@Test
	public void javaWithMavenUseJpaFacetDoesNotHaveJpaKotlinPlugin() {
		applyJpaMetadata(true);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("maven-project");
		request.setLanguage("java");
		generateMavenPom(request).doesNotContain("<plugin>jpa</plugin>")
				.doesNotContain("kotlin-maven-noarg");
	}

	@Test
	public void kotlinWithGradleUseJpaFacetHasJpaKotlinPlugin() {
		applyJpaMetadata(true);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("gradle-project");
		request.setLanguage("kotlin");
		generateGradleBuild(request).contains("apply plugin: 'kotlin-jpa'");
	}

	@Test
	public void kotlinWithGradleWithoutJpaFacetDoesNotHaveJpaKotlinPlugin() {
		applyJpaMetadata(false);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("gradle-project");
		request.setLanguage("kotlin");
		generateGradleBuild(request).doesNotContain("apply plugin: 'kotlin-jpa'");
	}

	@Test
	public void javaWithGradleUseJpaFacetDoesNotHaveJpaKotlinPlugin() {
		applyJpaMetadata(true);
		ProjectRequest request = createProjectRequest("data-jpa");
		request.setType("gradle-project");
		request.setLanguage("java");
		generateGradleBuild(request).doesNotContain("apply plugin: 'kotlin-jpa'");
	}

	private void applyJpaMetadata(boolean enableJpaFacet) {
		Dependency jpa = Dependency.withId("data-jpa");
		if (enableJpaFacet) {
			jpa.setFacets(Collections.singletonList("jpa"));
		}
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("data-jpa", jpa).build();
		applyMetadata(metadata);
	}

}
