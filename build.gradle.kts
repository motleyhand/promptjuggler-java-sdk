plugins {
    `java-library`
}

group = "com.promptjuggler"
version = "0.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // The generated client (com.promptjuggler.client) targets java.net.http (JDK built-in)
    // with Jackson serialization — these are its compile/runtime needs.
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.10")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    // The generated client isn't written for strict doclint; don't fail the build on it.
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}
