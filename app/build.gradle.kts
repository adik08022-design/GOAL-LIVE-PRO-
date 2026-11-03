plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.goallive.pro"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.goallive.pro"
        minSdk = 29
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
}
