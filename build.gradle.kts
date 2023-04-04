plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "biz.thehacker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("biz.thehacker.airq.MainKt")
}
