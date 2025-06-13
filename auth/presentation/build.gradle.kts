plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.auth.presentation"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.auth.domain)
}