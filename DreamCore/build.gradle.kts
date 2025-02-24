import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))
    compile(files("../libs/patched_1.15.1.jar"))
    compileOnly(files("../libs/ProtocolSupport.jar"))
    compileOnly("com.comphenix.protocol:ProtocolLib:4.4.0-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.0.0-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-legacy:7.0.0-SNAPSHOT")
    compileOnly("com.github.TechFortress:GriefPrevention:16.11.5")
    compileOnly("org.bstats:bstats-bukkit:1.2")
    implementation("net.perfectdreams.commands:command-framework-core:0.0.4")
    implementation("com.github.dmulloy2:PacketWrapper:84d4c3d9f1")
    compileOnly("net.milkbowl.vault:VaultAPI:1.6")
    compileOnly("com.meowj:LangUtils:1.9")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("org.mongodb:mongo-java-driver:3.7.0-rc0")
    implementation("com.zaxxer:HikariCP:2.7.8")
    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.jetbrains.exposed:exposed:0.10.5")
    implementation("com.okkero.skedule:skedule:1.2.4.1-SNAPSHOT")
    implementation("com.github.kevinsawicki:http-request:6.0")
    implementation("com.oracle.graaljs:graal-js:1.0.0-rc9")
    implementation("com.oracle.tregex:tregex:1.0.0-rc9")
    implementation("com.oracle.truffle:truffle-api:1.0.0-rc7")
    implementation("org.graalvm:graal-sdk:1.0.0-rc7")
    implementation("org.ow2.asm:asm:7.0")
    implementation("org.ow2.asm:asm-util:7.0")
    implementation("commons-codec:commons-codec:1.12")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation("club.minnced:discord-webhooks:0.1.5")
    implementation("com.github.ben-manes.caffeine:caffeine:2.6.2")
    compileOnly("com.greatmancode:craftconomy3:3.3.1")
    compileOnly("me.lucko.luckperms:luckperms-api:4.3")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.0-M1")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.3.0-M1")
    testCompile("io.mockk:mockk:1.9")
    testCompile("org.assertj:assertj-core:3.10.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("DreamCore-shadow")

        relocate("org.mongodb", "net.perfectdreams.libs.org.mongodb")
        relocate("com.mongodb", "net.perfectdreams.libs.com.mongodb")
        relocate("org.bson", "net.perfectdreams.libs.org.bson")
        relocate("co.aikar.commands", "net.perfectdreams.libs.acf")

        exclude {
            it.file?.name?.startsWith("kotlin") == true || it.file?.name?.startsWith("patched_") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}