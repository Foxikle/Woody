plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("maven-publish")
}

group = "me.flame.menus"
version = "3.0.0-beta11"

subprojects {
    apply(plugin= "java")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        maven("https://jitpack.io")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
        compileOnly("com.google.errorprone:error_prone_annotations:2.19.1")
        annotationProcessor("com.google.errorprone:error_prone_annotations:2.19.1")

        compileOnly("com.mojang:authlib:1.5.21")
        compileOnly("net.kyori:adventure-api:4.7.0")
        compileOnly("net.kyori:examination-api:1.3.0")
    }

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

apply(plugin="java")

tasks.shadowJar {
    archiveClassifier.set("")
}

publishing {
    repositories {
        maven {
            name = "mainRepository"
            url = uri("https://repo.foxikle.dev/flameyos")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks["shadowJar"])
        }
    }
}
