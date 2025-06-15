plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.run.network"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.bundles.koin)

    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)
}