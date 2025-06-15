plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.muhammad.core.database"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.org.mongodb.bson)
    implementation(libs.bundles.koin)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.room)
}
room {
    schemaDirectory("$projectDir/schemas")
}