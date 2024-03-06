plugins {
    id("java")
}

group = "maankoe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.slf4j:slf4j-simple:2.0.10")
    implementation("org.assertj:assertj-core:3.23.1")
    implementation("org.mockito:mockito-core:4.8.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
