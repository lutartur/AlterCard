plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.alterpay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.alterpay"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-ktx:1.12.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")

    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    val lifecycle_version = "2.10.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // CameraX and ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    val camerax_version = "1.5.3"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    // ZXing for barcode generation
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
