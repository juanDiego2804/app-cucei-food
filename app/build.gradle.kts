plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cuceifood"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cuceifood"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig=true

    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.10.1")//Para parsear la respuesta JSON a objetos Java:
    implementation("com.backendless:backendless:6.3.0")




// Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")





// Opcional para manejar tokens JWT
    implementation("com.auth0.android:jwtdecode:2.0.2")

    implementation("com.google.android.material:material:1.6.0")




// Dependencias básicas de Android
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0") // Material Components
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Componentes de navegación
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")







}