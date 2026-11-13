plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.favorites"
}

dependencies {
    implementation(project(":core:favorites"))
}
