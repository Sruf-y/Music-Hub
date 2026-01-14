import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    //id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" // or match your Kotlin version
    kotlin("plugin.serialization") version "2.2.0"
}

android {
    namespace = "com.aacomposetest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aacomposetest"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }


}
kotlin{
    compilerOptions{
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    implementation(libs.androidx.compose.material.icons.extended)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("com.composables:core:1.33.0")
    debugImplementation(libs.ui.tooling)


    //Room
    val room_version = "2.7.2"

    implementation("androidx.room:room-runtime:$room_version")

    implementation("androidx.room:room-ktx:$room_version")

    implementation("androidx.room:room-paging:$room_version")

    ksp("androidx.room:room-compiler:$room_version")


    //Coil
    implementation(libs.coil.compose)

    //Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.compose)

    //Media
    implementation("androidx.media:media:1.7.1")

    //Gson
    implementation("com.google.code.gson:gson:2.11.0")

    //Animation
    implementation("androidx.compose.animation:animation-graphics:1.9.1")

    //Navigation
    implementation("androidx.navigation:navigation-compose:2.9.5")

    //Service
    implementation("androidx.lifecycle:lifecycle-service:2.9.4")

    //Animation
    implementation("androidx.compose.animation:animation-core:1.9.3")

    //Reordering
    implementation(libs.reorderable)

    // Networking
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-android:3.0.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // SLF4J No-Op implementation
    implementation("org.slf4j:slf4j-nop:2.0.13")

    // ColorPicker
    implementation("com.github.skydoves:colorpicker-compose:1.0.0")
}