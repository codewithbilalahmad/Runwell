plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.core.notification"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.koin)
    implementation(projects.core.domain)
    implementation(projects.core.presentation.ui)
    implementation(projects.core.presentation.designsystem)
}