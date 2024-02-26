import java.net.URL

plugins {
    kotlin("jvm") version "1.9.22"
    id("xyz.jpenilla.run-paper") version "2.2.3"
}

group = "cz.coffeerequired"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = URL("https://repo.papermc.io/repository/maven-public/"))
    maven(url = URL("https://repo.skriptlang.org/releases"))
}

dependencies {
    compileOnly("org.eclipse.jdt:org.eclipse.jdt.annotation:2.0.0")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:2.8.2")
    compileOnly("com.sun.activation:jakarta.activation:2.0.1")
    compileOnly("com.sun.mail:jakarta.mail:2.0.1")
    compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    compileOnly("com.sanctionco.jmail:jmail:1.6.2")
    compileOnly("com.pivovarit:throwing-function:1.5.1")
    compileOnly("org.eclipse.angus:angus-mail:2.0.2")
    compileOnly("org.simplejavamail:simple-java-mail:8.6.3")
    compileOnly("org.simplejavamail:core-module:8.6.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks {
    kotlin {
        jvmToolchain(17)
    }

    processResources {
        filesMatching(listOf("plugin.yml", "lang/default.lang")) {
            expand(
                "version" to version,
                "kotlin_coroutines" to "1.8.0",
                "kotlin_version" to "1.9.22"
            )
        }
    }

    runServer {
        minecraftVersion("1.20.4")

        downloadPlugins {
            github("SkriptLang", "Skript", "2.8.2", "Skript.jar")
        }
    }
}
