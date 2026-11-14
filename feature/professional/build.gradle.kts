plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.professional"
}

dependencies {
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(project(":core:media"))
    implementation(libs.androidx.activity.compose)
}
