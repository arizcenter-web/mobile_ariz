plugins {
    id("ariz.android.library")
    id("ariz.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tecsup.app_ariz.core.network"

    buildFeatures {
        buildConfig = true
    }

    // URL del backend para release. Se pasa con -ParizApiBaseUrl=https://api.ariz.pe/ (debe terminar en "/").
    // Sin el parámetro usa el backend de producción (Nginx quita /api antes de llegar a Nest).
    val releaseApiBaseUrl = providers.gradleProperty("arizApiBaseUrl").orElse("https://arizinmobiliaria.com/api/").get()

    buildTypes {
        // 10.0.2.2 = localhost de la maquina host desde el emulador. El backend escucha en :4000 (sin prefijo /api).
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:4000/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
        }
    }
}

dependencies {
    implementation(project(":core:common"))

    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.security.crypto)
}
