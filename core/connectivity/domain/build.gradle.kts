plugins {
    alias(libs.plugins.android.library)
}
android {
    namespace = "com.muhammad.core.connectivity.domain"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
}
