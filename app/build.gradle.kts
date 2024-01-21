plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "h.lillie.weartube"
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "h.lillie.weartube"
        minSdk = 30
        // noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    val core_version = "1.12.0"
    implementation("androidx.core:core-ktx:$core_version")

    // AppCompat
    val appcompat_version = "1.6.1"
    implementation("androidx.appcompat:appcompat:$appcompat_version")

    // ConstraintLayout
    val constraintlayout_version = "2.1.4"
    implementation("androidx.constraintlayout:constraintlayout:$constraintlayout_version")

    // Material
    val material_version = "1.11.0"
    implementation("com.google.android.material:material:$material_version")

    // Kotlinx Coroutines
    val kotlinx_coroutines_version = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")

    // Lifecycle
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-common:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    // Play Services Wearable
    val playserviceswearable_version = "18.1.0"
    implementation("com.google.android.gms:play-services-wearable:$playserviceswearable_version")

    // OkHttp
    val okhttp_version = "4.12.0"
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")

    // Okio
    val okio_version = "3.6.0"
    implementation("com.squareup.okio:okio:$okio_version")

    // Coil
    val coil_version = "2.5.0"
    implementation("io.coil-kt:coil:$coil_version")

    // Gson
    val gson_version = "2.10.1"
    implementation("com.google.code.gson:gson:$gson_version")

    // Media3
    val media3_version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3_version")
    implementation("androidx.media3:media3-datasource-okhttp:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")
    implementation("androidx.media3:media3-session:$media3_version")
    implementation("androidx.media3:media3-extractor:$media3_version")
    implementation("androidx.media3:media3-database:$media3_version")
    implementation("androidx.media3:media3-decoder:$media3_version")
    implementation("androidx.media3:media3-datasource:$media3_version")
    implementation("androidx.media3:media3-common:$media3_version")
}