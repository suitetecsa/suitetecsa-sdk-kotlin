import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    `maven-publish`
}

group = "com.github.suitetecsa"
version = "0.1.4"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("junit:junit:4.13.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}