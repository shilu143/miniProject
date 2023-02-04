plugins {
    id("java")
    application
}

group = "org.iitrpr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("info.picocli:picocli:4.7.1")
    annotationProcessor("info.picocli:picocli-codegen:4.7.1")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
    implementation("io.github.cdimascio:dotenv-java:2.3.2")
    implementation("org.mindrot:jbcrypt:0.4")
}

application {
    mainClass.set("org.iitrpr.App")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}