plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

}

android {
    namespace = "org.bkkz.lumaapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.bkkz.lumaapp"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        buildConfig = true
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
    //=== Implementation ===\\

    //Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.core:core-splashscreen:1.0.1")

    //ViewModel, Coroutines, Koin
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
    implementation("io.insert-koin:koin-android:4.1.1")

    //API Handling: OkHttp, Retrofit, Gson
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.google.code.gson:gson:2.10.1")

    //Credentials Manager
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //Firebase Services
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-auth")

    //EncryptedSharedPref
    implementation("androidx.security:security-crypto:1.0.0")

    //Lottie
    implementation("com.airbnb.android:lottie:6.6.9")

    //Room DB
    ksp("androidx.room:room-compiler:2.8.0")
    implementation("androidx.room:room-ktx:2.8.0")

    //Google Calendar API
    implementation ("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0"){
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.api-client:google-api-client-android:1.23.0"){
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    //Third-Party LIB
    implementation("io.github.afreakyelf:Pdf-Viewer:2.3.7")

    //=== Test Implementation ===\\
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}