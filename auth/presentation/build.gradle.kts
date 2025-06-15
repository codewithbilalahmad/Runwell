plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.auth.presentation"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.auth.domain)
    implementation(libs.androidx.core.ktx)
}