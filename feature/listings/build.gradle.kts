plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.listings"
}

dependencies {
    implementation(project(":core:catalog"))
    implementation(project(":core:favorites"))
    implementation(project(":core:leads"))
}
