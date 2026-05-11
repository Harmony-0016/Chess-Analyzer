plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.compose") version "1.6.11"
    // This line is CRITICAL for Kotlin 2.0.0
    kotlin("plugin.compose") version "2.0.0"
}

repositories {
    // 1. Check the most reliable store first
    mavenCentral()

    // 2. Check Google's store
    google()

    // 3. Add JitPack (a common backup for GitHub-hosted libraries like chesslib)
    maven("https://jitpack.io")

    // 4. Check the specific Compose store last
    maven("https://maven.pkg.jetbrains.space/public/p/compose/informative")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.github.bhlangonijr:chesslib:1.3.3")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}