/*
 * Copyright (c) 2024-2026 balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    java
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.balugaq.msua"
version = "0.1.4"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

toolchainManagement {
    jvm {
        javaRepositories {
            repository("temurin") {
                resolverClass.set(JavaToolchainResolver::class.java)
            }
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    // Core - Provided
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.SlimefunGuguProject:Slimefun4:2025.1")
    compileOnly("io.github.pylonmc:rebar:0.42.0-26.1")
    compileOnly("io.github.pylonmc:pylon:0.40.1-26.1")
    compileOnly("xyz.xenondevs.invui:invui:2.1.0")
    compileOnly("xyz.xenondevs.invui:invui-kotlin:2.1.0")

    // Tools etc. - Compile (will be shaded)
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("io.papermc:paperlib:1.0.8")

    // Provided annotations
    compileOnly("com.google.code.findbugs:annotations:3.0.1u2")
    compileOnly("org.projectlombok:lombok:1.18.46")
    compileOnly("org.jspecify:jspecify:1.0.0")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok:1.18.46")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:-removal")
    }

    shadowJar {
        archiveBaseName.set("MSUA")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        // Minimize JAR (equivalent to minimizeJar=true)
        minimize()

        // Relocations
        relocate("io.papermc.lib", "com.balugaq.msua.libraries.paperlib")

        // Exclude META-INF files
        exclude("META-INF/*")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")

        // Merge service files
        mergeServiceFiles()

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
        filesMatching("**/*.properties") {
            expand(project.properties)
        }
        filesMatching("tags/*.json") {
            expand(project.properties)
        }
    }

    build {
        dependsOn(shadowJar)
    }
}