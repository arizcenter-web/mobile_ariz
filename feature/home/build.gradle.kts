plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.home"
}

dependencies {
    implementation(project(":core:catalog"))
    implementation(project(":core:favorites"))
}
