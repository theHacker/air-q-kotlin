plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "biz.thehacker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val jacksonVersion = "2.14.2"

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("biz.thehacker.airq.MainKt")
}
