plugins {
    id("java")
    application
    id("jacoco")
}

group = "org.iitrpr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
    implementation("io.github.cdimascio:dotenv-java:2.3.2")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("de.vandermeer:asciitable:0.3.2")
    implementation("com.opencsv:opencsv:5.7.1")
    testImplementation("org.jacoco:org.jacoco.agent:0.8.8")
}

application {
    mainClass.set("org.iitrpr.App")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}