/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.8.1/userguide/building_java_projects.html
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "5.0.0"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    jcenter()
    maven(url="https://jitpack.io")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Discord
    implementation(group="com.github.DV8FromTheWorld", name="JDA", version="v4.2.0")
    implementation(group="org.slf4j", name="slf4j-simple", version="1.7.25")

    // Jackson
    implementation(group="com.fasterxml.jackson.core", name="jackson-databind", version="2.10.0")
    implementation(group="com.fasterxml.jackson.module", name="jackson-module-kotlin", version="2.9.0")}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("ffxiv.raidtool.AppKt")
}

project.setProperty("mainClassName", "ffxiv.raidtool.AppKt")


tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("raidtool")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ffxiv.raidtool.AppKt"))
        }

        val dir = File("bin")
        dir.delete()
        dir.mkdir()

        File("app/build/libs/raidtool-all.jar").copyTo(File("bin/Raidtool.jar"), true)
        File("app/src/main/resources/").copyRecursively(File("bin/"), true)
    }
}