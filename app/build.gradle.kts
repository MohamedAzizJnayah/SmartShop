plugins {
    // Plugins Android / Kotlin (via Version Catalog "libs")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Plugin Firebase (nécessaire pour lire google-services.json)
    id("com.google.gms.google-services")
}

android {
    // Package/namespace de l'application
    namespace = "com.example.smartshop"

    // SDK utilisé pour compiler
    compileSdk = 36

    defaultConfig {
        // Identifiant unique de l'app
        applicationId = "com.example.smartshop"

        // Compatibilité Android
        minSdk = 24
        targetSdk = 36

        // Versioning
        versionCode = 1
        versionName = "1.0"

        // Runner pour les tests instrumentés
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Minification/obfuscation (Proguard/R8)
            isMinifyEnabled = false

            // Fichiers Proguard
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Compatibilité Java
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // JVM target Kotlin
    kotlinOptions {
        jvmTarget = "11"
    }

    // Activation de Jetpack Compose
    buildFeatures {
        compose = true
    }
}

dependencies {

    // =========================================================
    //  AndroidX (core + lifecycle + activity)
    // =========================================================
    implementation(libs.androidx.core.ktx)                 // Extensions Kotlin (Context, etc.)
    implementation(libs.androidx.lifecycle.runtime.ktx)    // Lifecycle runtime + coroutines support
    implementation(libs.androidx.activity.compose)         // Activity pour Compose (setContent)

    // =========================================================
    //  Jetpack Compose (UI)
    // =========================================================
    implementation(platform(libs.androidx.compose.bom))    // BOM Compose : gère versions cohérentes
    implementation(libs.androidx.ui)                      // UI Compose de base
    implementation(libs.androidx.ui.graphics)             // Graphics Compose
    implementation(libs.androidx.ui.tooling.preview)      // Preview Compose (Android Studio)
    implementation(libs.androidx.material3)               // Material 3 (UI components)

    implementation(libs.androidx.navigation.runtime.android) // Navigation (runtime)
    implementation(libs.androidx.room.common.jvm)            // Room (common/jvm)

    // =========================================================
    // Tests unitaires & instrumentés
    // =========================================================
    testImplementation(libs.junit)                        // Tests unitaires (local JVM)

    androidTestImplementation(libs.androidx.junit)        // JUnit Android
    androidTestImplementation(libs.androidx.espresso.core)// Espresso UI tests

    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM Compose pour tests
    androidTestImplementation(libs.androidx.ui.test.junit4)       // Tests Compose

    debugImplementation(libs.androidx.ui.tooling)         // Tooling (preview/debug)
    debugImplementation(libs.androidx.ui.test.manifest)   // Manifest pour tests Compose

    // =========================================================
    //  Room (base de données locale)
    // =========================================================
    implementation("androidx.room:room-runtime:<latest>")  // Runtime Room
    implementation("androidx.room:room-ktx:<latest>")      // Extensions Kotlin (coroutines, Flow)
    // KAPT ou KSP (choisis 1)

    // =========================================================
    // 🔥 Firebase
    // =========================================================

    // BOM Firebase : une seule version gère les libs Firebase (auth/firestore/etc.)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Analytics Firebase (optionnel)
    implementation("com.google.firebase:firebase-analytics")

    // Auth Firebase (login/register)
    implementation("com.google.firebase:firebase-auth")

    // Firestore (base de données distante)
    implementation("com.google.firebase:firebase-firestore")

    // =========================================================
    //  Jetpack Compose - Navigation / ViewModel / Icons
    // =========================================================
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // ViewModel + Compose
    implementation("androidx.navigation:navigation-compose:2.7.0")         // Navigation Compose
    implementation("androidx.compose.material:material-icons-extended")    // Icônes Material étendues



  
}
