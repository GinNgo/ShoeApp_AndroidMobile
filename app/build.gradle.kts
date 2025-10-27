plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

//    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.shoesapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shoesapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.appcompat:appcompat:1.7.0")   // hoặc 1.6.1, tùy phiên bản
    implementation("com.google.android.material:material:1.12.0")  // để dùng Material UI
    implementation ("com.github.bumptech.glide:glide:4.16.0")

}