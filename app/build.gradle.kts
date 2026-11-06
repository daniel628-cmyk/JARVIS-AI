import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nexora.ai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nexora.ai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// sherpa-onnx is downloaded into app/libs by the GitHub Actions build.
// It is only added when the file exists, so the build keeps working
// until the workflow step is in place.
val sherpaAar = file("libs/sherpa-onnx-1.12.40.aar")

dependencies {
    if (sherpaAar.exists()) {
        implementation(files(sherpaAar))
    }
}
