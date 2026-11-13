plugins {
    id("ariz.android.application")
    id("ariz.android.compose")
    id("ariz.android.hilt")
}

android {
    namespace = "com.tecsup.app_ariz"

    defaultConfig {
        applicationId = "com.tecsup.app_ariz"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Host del sitio web: lo usa el intent-filter del enlace "restablecer contraseña".
        manifestPlaceholders["arizWebHost"] = providers.gradleProperty("arizWebHost").get()
        // http solo si el backend de release se indica con http:// (APK de prueba en red local).
        manifestPlaceholders["arizCleartext"] =
            providers.gradleProperty("arizApiBaseUrl").map { it.startsWith("http://") }.orElse(false).get().toString()
    }

    buildTypes {
        release {
            // APK de prueba instalable (-ParizSignWithDebug=true): lo firma con la llave de debug.
            // Para publicar de verdad hay que firmar con la llave de release del equipo.
            if (providers.gradleProperty("arizSignWithDebug").isPresent) {
                signingConfig = signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

// Inicio de sesión con Google: se activa al colocar app/google-services.json (Firebase Console).
// Sin el archivo la app compila igual y el botón de Google queda oculto.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

dependencies {
    // :app solo ensambla. Conoce todas las features y el core minimo necesario.
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:session"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))

    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:listings"))
    implementation(project(":feature:projects"))
    implementation(project(":feature:favorites"))
    implementation(project(":feature:agents"))
    implementation(project(":feature:account"))
    implementation(project(":feature:rewards"))
    implementation(project(":feature:investments"))
    implementation(project(":feature:info"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
