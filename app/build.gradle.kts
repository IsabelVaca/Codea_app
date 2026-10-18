plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mx.tec.codea"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "mx.tec.codea"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // the google maps key is secret, so it lives in local.properties (not in git)
        // as MAPS_API_KEY=... and the manifest reads it from here.
        // without a key the app still builds, but the map shows empty.
        val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
            .asText.orNull.orEmpty()
        val mapsApiKey = localProperties.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("MAPS_API_KEY=") }
            ?.substringAfter("=")
            .orEmpty()
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        // la dirección de la API. orden: -PapiBaseUrl=... en la línea de comandos, API_BASE_URL en
        // local.properties, y por último el emulador (10.0.2.2 es tu Mac vista desde el emulador).
        // con un celular físico por USB: http://localhost:8000/api/v1/ y adb reverse tcp:8000 tcp:8000
        val apiBaseUrl = providers.gradleProperty("apiBaseUrl").orNull
            ?: localProperties.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("API_BASE_URL=") }
                ?.substringAfter("=")
            ?: "http://10.0.2.2:8000/api/v1/"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // en release la API va por HTTPS. mientras no haya dominio, esta dirección es un recordatorio:
            // pon la real en local.properties como API_BASE_URL_RELEASE antes de generar el APK de entrega.
            val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
                .asText.orNull.orEmpty()
            val releaseUrl = localProperties.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("API_BASE_URL_RELEASE=") }
                ?.substringAfter("=")
                ?: "https://api.codea.invalid/api/v1/"
            buildConfigField("String", "API_BASE_URL", "\"$releaseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.maps.compose)
    implementation(libs.compose.material.icons)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.reorderable)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.okhttp.mockwebserver)
}