plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.search"
}

dependencies {
    implementation(project(":core:catalog"))
    implementation(project(":core:favorites"))
}
