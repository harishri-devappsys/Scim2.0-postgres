plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

allprojects {
	group = "com.valura"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// Add dependencies for the root project
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("org.postgresql:postgresql")
	testImplementation("com.h2database:h2")

	implementation(project(":database"))
	implementation(project(":scim"))

}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}