plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // 🆕 Para Firebase
}

android {
    namespace = "com.example.misLugares"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.misLugares"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // API key GraphHopper: en la raíz del proyecto crea/edita local.properties y añade: graphhopperApiKey=tu_clave
        val localFile = rootProject.file("local.properties")
        var ghKey = "4849b4d9-8656-4f1e-bc29-f1f79f045f27"
        if (localFile.exists()) {
            localFile.forEachLine { line ->
                if (line.startsWith("graphhopperApiKey=")) {
                    ghKey = line.removePrefix("graphhopperApiKey=").trim()
                }
            }
        }
        buildConfigField("String", "GRAPHHOPPER_API_KEY", "\"$ghKey\"")
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("org.osmdroid:osmdroid-android:6.1.18")


    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.7.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}