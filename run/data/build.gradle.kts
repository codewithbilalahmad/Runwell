plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.muhammad.run.data"
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.google.android.gms.play.services.location)
    implementation(libs.androidx.work)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.run.domain)
    implementation(projects.core.connectivity.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.koin)
}