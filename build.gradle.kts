import groovy.lang.Closure
import org.gradle.jvm.tasks.Jar
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

plugins {
    java
    alias(catalog.plugins.git.version)

    alias(catalog.plugins.unmined)
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val name: String by rootProject.properties
val description: String by rootProject.properties
val source: String by rootProject.properties

group = "snownee.boattweaks"

val gitVersion: Closure<String> by extra
version = gitVersion()

base {
    archivesName = archive_name
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
}

repositories {
    unimined.curseMaven()
    unimined.modrinthMaven()

    maven("https://maven.su5ed.dev/releases") {
        content {
            includeGroupAndSubgroups("dev.su5ed")
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://jitpack.io") {
        content {
            includeGroupAndSubgroups("com.github")
        }
    }
}

val mainImplementation by configurations.creating

unimined.minecraft {
    version(catalog.versions.minecraft.get())

    mappings {
        intermediary()
        mojmap()
        parchment(mcVersion = "1.21", version = "2024.07.28")

        devFallbackNamespace("intermediary")
    }

    if (sourceSet == sourceSets.main.get()) {
        fabric {
            loader(catalog.versions.fabric.loader.get())
        }
    }
}

val modImplementation by configurations.getting
val include by configurations.getting

dependencies {
    modImplementation(unimined.fabricApi.fabric(catalog.versions.fabric.api.get()))

    modImplementation(catalog.kiwi)
    modImplementation(catalog.boathud)
}

tasks {
    withType<ProcessResources> {
        val properties = mapOf(
                "id" to id,
                "version" to rootProject.version,
                "group" to rootProject.group,
                "name" to rootProject.name,
                "description" to rootProject.property("description").toString(),
                "source" to rootProject.property("source").toString(),
                "fabric_loader" to ">=0.15",
                "minecraft" to ">=1.21",
                "kiwi" to ">=15",
                "java" to ">=21"
        )
        from(rootProject.sourceSets.main.get().resources)
        inputs.properties(properties)

        filesMatching(
                listOf(
                        "fabric.mod.json",
                        "META-INF/neoforge.mods.toml",
                        "META-INF/mods.toml",
                        "*.mixins.json",
                        "META-INF/MANIFEST.MF"
                )
        ) {
            expand(properties)
        }
    }

    named<Jar>("sourcesJar") {
        from(sourceSets.map { it.allSource })

        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    named<RemapJarTask>("remapJar") {
        mixinRemap {
            disableRefmap()
        }
    }
}
