plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.auth"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Host del enlace "restablecer contraseña" (gradle.properties -> arizWebHost).
        buildConfigField("String", "WEB_HOST", "\"${providers.gradleProperty("arizWebHost").get()}\"")
    }
}

dependencies {
    // Inicio de sesión con Google: selector de cuentas (Credential Manager) + Firebase Auth.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
}
