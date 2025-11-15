plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.glaucoma.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.glaucoma.ai"
        minSdk = 24
        targetSdk = 35
        versionCode = 6
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Add 16 KB page size support
        ndk {
            // Play Store upload: ARM64 only
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    // Modern packaging ensures .so files are uncompressed & 16KB-aligned
    packaging {
        jniLibs {
            // modern packaging (don’t extract at install)
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.withType<com.android.build.gradle.internal.tasks.PackageBundleTask>().configureEach {
    doFirst {
        println(">>> Packaging task usingLegacyPackaging? ${this.name}")
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.dagger)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.play.services.location)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.gson)
    kapt(libs.dagger.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.lottie)
    implementation(libs.converter.gson)

    implementation(libs.android.spinkit)
    implementation(libs.photofilter)

    // LiteRT (successor to TensorFlow Lite) – 16KB compatible
    implementation("com.google.ai.edge.litert:litert:1.4.0")
    implementation("com.google.ai.edge.litert:litert-api:1.4.0")
    implementation("com.google.ai.edge.litert:litert-support:1.4.0")
    implementation("com.google.ai.edge.litert:litert-metadata:1.4.0")

}