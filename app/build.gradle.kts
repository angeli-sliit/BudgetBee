plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safe.args)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.hilt.android)
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

    kapt {
        correctErrorTypes = true
        useBuildCache = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
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
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.core)
    implementation(libs.androidx.hilt.common)
    implementation(libs.identity.jvm)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Other libraries
    implementation(libs.material.datetime.picker)
    implementation(libs.mp.android.chart)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-ktx:1.12.0")

    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.activity:activity-ktx:1.8.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.1")

    implementation ("com.google.code.gson:gson:2.10.1")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Image Loading
    implementation("io.coil-kt:coil:2.5.0")

    // Testing
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")

    // Material Design 3
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")

    // Material Design Icons
    implementation("androidx.compose.material:material-icons-core:1.6.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Animation
    implementation("androidx.compose.animation:animation:1.6.1")
    implementation("androidx.compose.animation:animation-graphics:1.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Shimmer Effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.3.0")

    // Coil for Image Loading
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Hilt Work Manager Integration
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
}