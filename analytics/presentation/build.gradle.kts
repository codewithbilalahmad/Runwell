plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.analytics.presentation"
}

dependencies {
    implementation(projects.analytics.domain)
}