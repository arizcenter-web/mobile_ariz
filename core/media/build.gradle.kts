plugins {
    id("ariz.android.library")
    id("ariz.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tecsup.app_ariz.core.media"
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:network"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
}
