plugins {
    id("com.android.application")
    // NOTE: with AGP 9 the Kotlin compiler is built in; do NOT apply
    // org.jetbrains.kotlin.android (it fails with a fatal diagnostic).
}

android {
    namespace = "eu.kanade.tachiyomi.extension.en.ehentai"

    compileSdk = 36

    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.ehentai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.6.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            // Debug-signed so the APK can be installed directly ("local install" in Mihon).
            // Keep rules are minimal because the source class is discovered by dex scanning.
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    signingConfigs {
        // Project-local debug key (AGP's default would write to the user home
        // directory, which is not writable in every build environment).
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    packaging {
        resources.excludes += "kotlin-tooling-metadata.json"
    }

    dependenciesInfo {
        includeInApk = false
    }
}

// Kotlin jvmTarget is derived automatically from compileOptions (AGP 9 built-in Kotlin).

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("tachiyomi-en.ehentai-v1.6.1.apk")
        }
    }
}

dependencies {
    // Provided by the Mihon app at runtime.
    compileOnly("com.github.keiyoushi:extensions-lib:6e0c96cea8")
    compileOnly("org.jsoup:jsoup:1.22.2")
    compileOnly("com.squareup.okhttp3:okhttp:5.4.0")
    compileOnly("com.github.null2264.injekt:injekt-core:4135455a2a")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    compileOnly("org.jspecify:jspecify:1.0.0")

    // Unit tests run on the JVM against the real classes.
    testImplementation("com.github.keiyoushi:extensions-lib:6e0c96cea8")
    testImplementation("org.jsoup:jsoup:1.22.2")
    testImplementation("com.squareup.okhttp3:okhttp:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    testImplementation("junit:junit:4.13.2")
}
