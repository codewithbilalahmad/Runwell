plugins {
    alias(libs.plugins.android.library)
}
android {
    namespace = "com.muhammad.core.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
}
