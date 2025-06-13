plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.muhammad.run.data"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.google.android.gms.play.services.location)
    implementation(libs.androidx.work)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.run.domain)
    implementation(projects.core.connectivity.domain)
}