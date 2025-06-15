plugins {
    alias(libs.plugins.androidDynamicFeature)
}
android {
    namespace = "com.muhammad.analytics.analytics_feature"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.navigation.compose)
    api(projects.analytics.presentation)
    implementation(projects.analytics.domain)
    implementation(projects.analytics.data)
    implementation(projects.core.database)
    implementation(libs.androidx.core.ktx)
}