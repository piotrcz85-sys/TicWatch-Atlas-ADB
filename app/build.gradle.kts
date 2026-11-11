plugins {
    id("com.android.application")
}

android {
    namespace = "pl.ticwatch.atlasadb"
    compileSdk = 36
    defaultConfig {
        applicationId = "pl.ticwatch.atlasadb"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.flyfishxu:kadb:2.1.1")
}
