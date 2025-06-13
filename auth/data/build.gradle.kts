plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.auth.data"
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(projects.auth.domain)
    implementation(projects.core.domain)
    implementation(projects.core.data)
}