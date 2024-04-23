plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("maven-publish")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

group = "me.flame.menus"
version = "3.0.0"

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
        compileOnly("org.projectlombok:lombok:1.18.32")
        compileOnly("com.mojang:authlib:1.5.21")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
    }
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
