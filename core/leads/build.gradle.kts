plugins {
    id("ariz.android.library")
    id("ariz.android.compose")
    id("ariz.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.tecsup.app_ariz.core.leads"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:session"))
    implementation(project(":core:designsystem"))

    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.mockito.core)
}
