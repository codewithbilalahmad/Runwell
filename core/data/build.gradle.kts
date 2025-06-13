plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.core.data"
}

dependencies {
    implementation(libs.bundles.koin)
    implementation(projects.core.domain)
    implementation(projects.core.database)
}