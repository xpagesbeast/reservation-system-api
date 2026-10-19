plugins {
    id("java")
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

// Configure git properties to not fail if no git repository exists
gitProperties {
    failOnNoGitDirectory = false
}

group = "com.reservation-system"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    
    // OpenTelemetry (to be configured in future change)
    // implementation("io.opentelemetry:opentelemetry-api:1.42.0")
    // implementation("io.opentelemetry:opentelemetry-sdk:1.42.0")
    // implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.42.0")
    // implementation("io.opentelemetry:opentelemetry-exporter-logging:1.42.0")
    
    // JSON Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    
    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    
    // H2 for development and testing
    implementation("com.h2database:h2")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:mysql:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    options.header("Lane Reservation API")
    options.windowTitle("Lane Reservation API Documentation")
}

// Copy JavaDoc to static resources for serving via Spring Boot
tasks.register<Copy>("copyJavadocToStatic") {
    dependsOn(tasks.javadoc)
    from(tasks.javadoc.get().destinationDir)
    into("src/main/resources/static/javadoc")
}

// Ensure JavaDoc is copied before building
tasks.bootJar {
    dependsOn("copyJavadocToStatic")
    archiveFileName.set("reservation-system.jar")
}

// Generate build-info for actuator info endpoint
springBoot {
    buildInfo()
}