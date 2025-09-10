import com.github.gradle.node.npm.task.NpmTask

plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.node-gradle.node") version "7.1.0"
}

node {
	version.set("22.19.0")
	npmVersion.set("10.9.3")
	download.set(true)
}

group = "de.jadenk"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
	gradlePluginPortal()
}

tasks.register<NpmTask>("tailwindBuild") {
	group = "build"
	description = "Build Tailwind CSS in-place"
	args.set(listOf("run", "build:css")) // equivalent to 'npm run build:css'
}

tasks.named("processResources") {
	dependsOn("tailwindBuild")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	implementation("org.json:json:20240303")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core:5.12.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
	testImplementation("org.springframework.boot:spring-boot-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
