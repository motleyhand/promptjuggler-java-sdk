plugins {
    `java-library`
    id("com.diffplug.spotless") version "7.0.2"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

spotless {
    java {
        target("src/**/*.java")
        // The generated client carries its own style and is verified by the drift-check.
        targetExclude("src/main/java/com/promptjuggler/client/**")
        // Eclipse JDT formatter — self-contained (no jdk.compiler internals), so it runs on
        // current JDKs where google-java-format / palantir-java-format break. The profile is
        // Google's published Eclipse style (2-space, same output as google-java-format).
        eclipse().configFile("eclipse-format.xml")
        importOrder()
    }
}

group = "com.promptjuggler"
version = "0.0.0" // stamped from the release tag by sdk-java-publish.yaml

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    // sources + javadoc jars are added by the maven-publish plugin (Maven Central requires them).
}

dependencies {
    // JSpecify nullness annotations on the hand-written facade. compileOnly: they live in
    // the bytecode for downstream null-checkers but impose no transitive dependency.
    compileOnly("org.jspecify:jspecify:1.0.0")

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

mavenPublishing {
    // Targets the Central Portal (central.sonatype.com); legacy OSSRH is retired and the
    // SonatypeHost argument was dropped in 0.34+ — Central Portal is now the only target.
    publishToMavenCentral()
    // Maven Central requires GPG-signed artifacts. Keys come from the signingInMemoryKey*
    // Gradle properties, fed via ORG_GRADLE_PROJECT_* env vars in the publish workflow.
    signAllPublications()

    coordinates("com.promptjuggler", "promptjuggler-java", version.toString())

    pom {
        name.set("PromptJuggler Java SDK")
        description.set("Official Java SDK for the PromptJuggler API.")
        url.set("https://github.com/motleyhand/promptjuggler-java-sdk")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("motleyhand")
                name.set("PromptJuggler")
                url.set("https://promptjuggler.com")
            }
        }
        scm {
            url.set("https://github.com/motleyhand/promptjuggler-java-sdk")
            connection.set("scm:git:git://github.com/motleyhand/promptjuggler-java-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/motleyhand/promptjuggler-java-sdk.git")
        }
    }
}
