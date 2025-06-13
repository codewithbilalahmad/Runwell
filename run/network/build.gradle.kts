plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.run.network"
}

dependencies {
    implementation(libs.bundles.koin)

    implementation(projects.core.domain)
    implementation(projects.core.data)
}