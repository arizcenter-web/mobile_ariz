plugins {
    id("ariz.android.library")
    id("ariz.android.compose")
}

android {
    namespace = "com.tecsup.app_ariz.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
}
