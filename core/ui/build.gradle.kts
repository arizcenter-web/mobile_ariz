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
    implementation(libs.coil.compose)
}
