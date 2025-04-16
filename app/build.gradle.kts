plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safe.args)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.budgetbee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.budgetbee"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)

    // Gson for JSON
    implementation(libs.gson)

    // Firebase Crashlytics
    implementation(libs.firebase.crashlytics.buildtools)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Data Binding (no need for explicit version with AGP 8.7.3)
    kapt("androidx.databinding:databinding-compiler:8.7.3")

    // Other libraries
    implementation(libs.material.datetime.picker)
    implementation(libs.mp.android.chart)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-ktx:1.12.0")
}