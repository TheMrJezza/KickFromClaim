plugins {
    id("java")
}

group = "dev.themrjezza.kickfromclaim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("com.github.GriefPrevention:GriefPrevention:16.18.5")
    compileOnly("org.spigotmc:spigot-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}