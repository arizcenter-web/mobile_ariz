plugins {
    id("ariz.android.library")
    id("ariz.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tecsup.app_ariz.core.catalog"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    implementation(project(":core:network"))

    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
}
