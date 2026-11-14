plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.customer"
}

dependencies {
    implementation(project(":core:favorites"))
    implementation(project(":core:media"))
    implementation(libs.androidx.activity.compose)
}
