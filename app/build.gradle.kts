plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.anoop.myprojects.todoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anoop.myprojects.todoapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "8.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "banner_ad_unit_id_1",
                "ca-app-pub-3940256099942544/6300978111"
            )
            resValue(
                "string",
                "banner_ad_unit_id_2",
                "ca-app-pub-3940256099942544/6300978111"
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue(
                "string",
                "banner_ad_unit_id_1",
                "ca-app-pub-9749303862760552/7648755180"
            )
            resValue(
                "string",
                "banner_ad_unit_id_2",
                "ca-app-pub-9749303862760552/3444758626"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.gson)
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}