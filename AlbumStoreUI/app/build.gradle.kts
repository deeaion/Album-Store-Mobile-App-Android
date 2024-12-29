plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    id("kotlin-parcelize")

}

android {
    namespace = "com.albumstore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.albumstore"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.converter.gson)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.play.services.nearby)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.rendering)
    implementation(libs.sceneform.ux)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    // Data store
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation (libs.accompanist.swiperefresh)
    implementation (libs.signalr)
    implementation (libs.moshi)
    implementation (libs.moshi.kotlin)
    implementation (libs.androidx.foundation)
    implementation (libs.material3)
    implementation (libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.maps.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.camera.camera2)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)

    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.camera:camera-extensions:1.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Replace with the latest version
    //parcelize
//    implementation("org.jetbrains.kotlinx:kotlinx-parcelize:1.5.2")
    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.9.10")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4") // Use the latest version
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("io.coil-kt:coil-compose:2.2.2")
//    implementation("androidx.paging:paging-compose:1.0.0")

}