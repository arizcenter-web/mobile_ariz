plugins {
    id("ariz.android.feature")
}

android {
    namespace = "com.tecsup.app_ariz.feature.agents"
}

dependencies {
    implementation(project(":core:favorites"))
}
