dependencyResolutionManagement {
    pluginManagement {
        repositories {
            maven("https://maven.wagyourtail.xyz/releases")
            mavenCentral()
            gradlePluginPortal()
        }
    }
}

dependencyResolutionManagement.versionCatalogs.create("catalog") {
    // https://github.com/palantir/gradle-git-version
    plugin("git-version", "com.palantir.git-version").version("3.+")

    plugin("shadow", "com.gradleup.shadow").version("8.+")

    plugin("unmined", "xyz.wagyourtail.unimined").version("1.+")

    val minecraft = "1.21.1"
    version("minecraft", minecraft)

    // https://linkie.shedaniel.dev/dependencies?loader=fabric
    version("fabric-loader", "0.16.7")
    version("fabric-api", "0.106.0+$minecraft")

    // https://linkie.shedaniel.dev/dependencies?loader=neoforge
    version("neoforge", "21.1.72")
    // https://modrinth.com/mod/forgified-fabric-api/versions
    library("forgified-fabric-api", "dev.su5ed.sinytra.fabric-api", "fabric-api").version("0.104.0+2.0.15+$minecraft")
    // https://modrinth.com/mod/connector/versions
    library("sinytra-connector", "org.sinytra", "Connector").version("2.0.0-beta.3+$minecraft")

    library("mixin", "org.spongepowered", "mixin").version("0.8.7")
    val mixinextras = "0.4.1"
    library("mixinextras-common", "io.github.llamalad7", "mixinextras-common").version(mixinextras)
    library("mixinextras-lexforge", "io.github.llamalad7", "mixinextras-forge").version(mixinextras)
    library("mixinextras-fabric", "io.github.llamalad7", "mixinextras-fabric").version(mixinextras)

    library("autocodec", "com.github.Builderb0y", "AutoCodec").version("5.0.0")

    library("kiwi", "maven.modrinth", "kiwi").version("15.1.1+fabric")

    library("boathud", "maven.modrinth", "boat-hud-extended").version("1.1.0")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val name: String by settings

rootProject.name = name
