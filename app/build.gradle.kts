plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.sdkqa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sdkqa"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation("io.github.mediastream:mediastreamplatformsdkandroid:9.9.1-alpha06")

    // EventBus for service communication
    implementation("org.greenrobot:eventbus:3.3.1")

    // Media3 session for background playback
    implementation("androidx.media3:media3-session:1.4.0")
    implementation("androidx.media3:media3-ui:1.4.0")
    
    // Core Library Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    
    // UiAutomator para captura de screenshots
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    
    // Allure para reportes avanzados
    androidTestImplementation("io.qameta.allure:allure-kotlin-android:2.4.0")
    androidTestImplementation("io.qameta.allure:allure-kotlin-commons:2.4.0")
    androidTestImplementation("io.qameta.allure:allure-kotlin-junit4:2.4.0")
}