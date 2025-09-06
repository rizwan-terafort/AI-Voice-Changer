import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
//    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)
    alias(libs.plugins.navigation.safe.args)
}

android {
    namespace = "com.voicechanger.funnysound"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.voicechanger.funnysound"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "Voice Changer-" + versionName)
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.dagger.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.singular.sdk)
    implementation(libs.mlkit.segmentation.selfie)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.audiovisualizer)

    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.instavision.ffmpegkit)
    implementation(libs.lottie)

    implementation(libs.datastore.preference)
    implementation(libs.datastore.core)


}