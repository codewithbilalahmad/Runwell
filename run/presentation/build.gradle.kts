plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.muhammad.run.presentation"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.google.maps.android.compose)
    implementation(libs.androidx.activity.compose)
    implementation(projects.core.domain)
    implementation(projects.run.domain)
    implementation(projects.core.connectivity.domain)
    implementation(projects.core.notification)
}