import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.compose") version "1.6.11"
    kotlin("plugin.serialization") version "1.9.24"
}

repositories {
    google()
    mavenCentral()
    // Compose Multiplatform repository
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "StudentDispatch"
            packageVersion = "1.0.0"

            windows {
                // makes MSI/EXE use a consistent start menu folder
                menuGroup = "StudentDispatch"
                upgradeUuid = "2f7c7f29-1a4f-4c5c-9c3e-7e8b3c8c8f31"
            }
        }
    }
}
