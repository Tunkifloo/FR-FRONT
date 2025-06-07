plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

android {
    namespace = "com.example.fr_front"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fr_front"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose BOM - versión actualizada y compatible
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // Jetpack Compose core - las versiones ahora las maneja el BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Activity Compose - versión actualizada
    implementation("androidx.activity:activity-compose:1.9.2")

    // Navegación Compose - versión actualizada
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // ViewModel Compose - versión actualizada
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Accompanist - versiones compatibles con Compose actual
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Networking - Retrofit - versiones actuales
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines - versión actualizada
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Coil para carga de imágenes - versión actualizada
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Animaciones Lottie - versiones actuales
    implementation("com.airbnb.android:lottie:6.5.2")
    implementation("com.airbnb.android:lottie-compose:6.5.2")

    // MotionLayout para Compose - versión estable
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Core KTX - versión actualizada
    implementation("androidx.core:core-ktx:1.13.1")

    // Material Icons Extended - gestionado por BOM
    implementation("androidx.compose.material:material-icons-extended")

    // Librerías base de Android - usar las del catálogo de versiones
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Testing - versiones específicas para evitar errores de resolución
    testImplementation(libs.junit)
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.4")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.4")

    // Debug tools - gestionados por BOM
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}