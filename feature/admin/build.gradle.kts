plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.admin"
}

dependencies {
    implementation(project(":core:media"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
}
