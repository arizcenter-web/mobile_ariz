plugins {
    id("ariz.android.library")
    id("ariz.android.hilt")
}

android {
    namespace = "com.tecsup.app_ariz.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)
}
