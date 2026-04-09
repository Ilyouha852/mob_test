plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.greenrobot.greendao")
}

android {
    namespace = "com.example.mobdev_lab3"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.mobdev_lab3"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

greendao {
    schemaVersion = 2
    daoPackage = "com.example.mobdev_lab3.database.dao"
    targetGenDir = file("src/main/java")
}

configurations.all {
    resolutionStrategy {
        force("androidx.activity:activity:1.6.1")
        force("androidx.activity:activity-ktx:1.6.1")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Явно указываем версию activity, совместимую с compileSdk 33
    implementation("androidx.activity:activity-ktx:1.6.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.8.0")

    // Gson для работы с JSON в SharedPreferences
    implementation("com.google.code.gson:gson:2.10.1")

    // GreenDAO для работы с SQLite
    implementation("org.greenrobot:greendao:3.3.0")

    // Coroutines для асинхронных операций
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // OkHttp — HTTP-клиент с поддержкой HTTP/2, кэширования, таймаутов
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Retrofit — типобезопасный HTTP-клиент поверх OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Fix for Gradle 7+ and greenDAO: explicit dependency
tasks.whenTaskAdded {
    if (name == "compileDebugKotlin" || name == "compileReleaseKotlin") {
        dependsOn("greendao")
    }
}
