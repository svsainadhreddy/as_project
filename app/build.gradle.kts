plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")


}

android {
    namespace = "com.example.myapplicationpopc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplicationpopc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // ✅ Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ✅ OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // ✅ Gson (optional, Retrofit already includes it via converter)
    implementation("com.google.code.gson:gson:2.8.9")


    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

}
