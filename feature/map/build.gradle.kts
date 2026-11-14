plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.map"
}

dependencies {
    implementation(project(":core:catalog"))
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
}
