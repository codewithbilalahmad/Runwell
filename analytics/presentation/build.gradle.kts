plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.analytics.presentation"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(projects.analytics.domain)
    implementation(libs.androidx.core.ktx)
}