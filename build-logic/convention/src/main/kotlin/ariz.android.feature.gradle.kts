plugins {
    id("ariz.android.library")
    id("ariz.android.compose")
    id("ariz.android.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", project(":core:common"))
    add("implementation", project(":core:model"))
    add("implementation", project(":core:network"))
    add("implementation", project(":core:session"))
    add("implementation", project(":core:designsystem"))
    add("implementation", project(":core:ui"))

    add("implementation", libs.findLibrary("androidx-navigation-compose").get())
    add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
    add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    add("implementation", libs.findLibrary("kotlinx-serialization-json").get())
    add("implementation", libs.findLibrary("retrofit").get())
    add("implementation", libs.findLibrary("retrofit-kotlinx-serialization").get())
    add("implementation", libs.findLibrary("coil-compose").get())

    add("testImplementation", libs.findLibrary("mockito-core").get())
}
