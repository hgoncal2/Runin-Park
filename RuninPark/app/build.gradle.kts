plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    signingConfigs {
        create("config") {
            storeFile = file("C:\\Users\\hugo2\\runinPark.jks")
            storePassword = "hugo2023"
            keyAlias = "keyR"
            keyPassword = "hugo2023"
        }
    }
    namespace = "com.ipt.runinpark"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ipt.runinpark"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("config")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    //Gson
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation("androidx.fragment:fragment-ktx:1.3.2")
    //OKHTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    //MaskedEditText
    implementation("io.github.vicmikhailau:MaskedEditText:5.0.1")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}