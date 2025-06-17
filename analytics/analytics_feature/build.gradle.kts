plugins {
    alias(libs.plugins.androidDynamicFeature)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.muhammad.analytics.analytics_feature"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.navigation.compose)
    api(projects.analytics.presentation)
    implementation(projects.analytics.domain)
    implementation(projects.analytics.data)
    implementation(projects.core.database)
    implementation(projects.core.presentation.designsystem)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.koin)
}