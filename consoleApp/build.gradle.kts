plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.sandeep.consoleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sandeep.consoleapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
    // Removed org.json to avoid conflict with Android's built-in org.json classes.
    // If you need an external JSON library, use Gson/Moshi/kotlinx.serialization instead:
    implementation("com.google.code.gson:gson:2.10.1")

    // Hilt runtime + compiler
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
