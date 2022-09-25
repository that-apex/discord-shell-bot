import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.thatapex.shellbot"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}


dependencies {
    implementation("dev.kord:kord-core:0.8.0-M16")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")

    implementation("com.google.cloud:google-cloud-compute:1.12.1")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.bouncycastle:bcprov-jdk16:1.46")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(mapOf("Main-Class" to "net.thatapex.shellbot.MainKt"))
    }
}
