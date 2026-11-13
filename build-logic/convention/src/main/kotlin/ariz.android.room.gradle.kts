plugins {
    id("com.google.devtools.ksp")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", libs.findLibrary("androidx-room-runtime").get())
    add("implementation", libs.findLibrary("androidx-room-ktx").get())
    add("ksp", libs.findLibrary("androidx-room-compiler").get())
}
