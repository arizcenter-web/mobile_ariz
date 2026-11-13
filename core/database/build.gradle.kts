plugins {
    id("ariz.android.library")
    id("ariz.android.hilt")
    id("ariz.android.room")
}

android {
    namespace = "com.tecsup.app_ariz.core.database"
}

// Room: cache offline de listados y favoritos. La @Database y los DAOs se agregan aqui cuando
// exista la primera entidad (Room exige al menos una).
dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.runtime)
}
