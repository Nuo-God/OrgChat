
plugins {
    id("com.android.application")
    
}

android {
    namespace = "com.dream.orgchat"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "com.dream.orgchat"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        
    }
    
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "src/main/libs", "include" to listOf("*.jar"))))
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.8.8") // Gson
    implementation("com.squareup.okhttp3:okhttp:4.9.1") // OkHttp
    implementation("org.luaj:luaj-jse:3.0.1") // Luaj
    implementation("com.google.android.gms:play-services-maps:18.0.2") // Google Maps
}
