buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP 9 has built-in Kotlin support; the compiler version is taken
        // from the kotlin-gradle-plugin on the buildscript classpath.
        // Must match the metadata version of the extensions-lib (Kotlin 2.4.x).
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    id("com.android.application") version "9.3.1" apply false
}
