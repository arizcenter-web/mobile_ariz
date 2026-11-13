plugins {
    id("ariz.android.library")
    id("ariz.android.compose")
}

android {
    namespace = "com.tecsup.app_ariz.core.ui"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    api(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
}
