plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.analytics.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.koin)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.analytics.domain)
    implementation(libs.androidx.core.ktx)
}