plugins {
        id("com.android.application")
        id("com.google.gms.google-services")
}

android {
    namespace = "com.example.styletimeandroidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.styletimeandroidapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // הוספת ספריות נדרשות
    implementation("com.google.android.gms:play-services-safetynet:17.0.1")  // עבור reCAPTCHA
    implementation("com.google.android.gms:play-services-auth:20.7.0")       // עבור Google Play Services
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")   // עבור refresh מסך
    implementation("com.google.firebase:firebase-analytics")                  // עבור Firebase Analytics
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.android.gms:play-services-base:18.5.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
