plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.projects"
}

dependencies {
    implementation(project(":core:catalog"))
    implementation(project(":core:leads"))
}
