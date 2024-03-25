plugins {
    kotlin("jvm") version "1.8.22"
    id("fabric-loom") version "1.4-SNAPSHOT"
    kotlin("plugin.serialization") version "1.8.22"
    `maven-publish`
    java
}

val mcVersion = "1.20.4"
val silkVersion = "1.10.3"

group = "gg.norisk"
version = "${mcVersion}-1.1.0"

repositories {
    mavenCentral()
    maven("https://maven.kosmx.dev/")
    maven("https://maven.wispforest.io")
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.3")
    modImplementation("net.fabricmc:fabric-loader:0.15.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.91.3+$mcVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.16+kotlin.1.9.21")

    include(modImplementation("net.silkmc:silk-commands:$silkVersion")!!)
    include(modImplementation("net.silkmc:silk-core:$silkVersion")!!)
    include(modImplementation("net.silkmc:silk-network:$silkVersion")!!)

    modImplementation("software.bernie.geckolib:geckolib-fabric-$mcVersion:4.3.1")
    modImplementation("dev.kosmx.player-anim:player-animation-lib-fabric:1.0.2-rc1+1.20")
    modImplementation("io.wispforest:owo-lib:0.12.5+1.20.3")
    modCompileOnly("maven.modrinth:iris:1.6.17+1.20.4")
}

loom {
    accessWidenerPath.set(file("src/main/resources/subwaysurfers.accesswidener"))
}

tasks {

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            // mavenLocal()
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileJava {
        sourceCompatibility = "17"
    }

}

java {
    withSourcesJar()
}
