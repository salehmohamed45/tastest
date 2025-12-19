plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.depi.drlist"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.depi.drlist"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // للتعامل مع ViewModel في Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")

// للتعامل مع StateFlow بشكل متوافق مع دورة حياة الواجهة
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")

// مكتبة الأيقونات
    implementation("androidx.compose.material:material-icons-extended")

// لتحويل Callbacks الخاصة بـ Firebase إلى Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // Firebase BoM (يحدد لك الإصدارات المتوافقة)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")


    // Firebase services (KTX suffix is no longer needed)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}