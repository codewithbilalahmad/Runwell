plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.muhammad.core.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.bundles.ktor)
}