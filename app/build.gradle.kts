import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.crashlytics)
    id("com.google.gms.google-services")
    alias(libs.plugins.compose.compiler)

}

android {
    namespace = "com.example.androiddevops"
    compileSdk = 35

    val versionPropertiesFile = file("version.properties")
    if (versionPropertiesFile.canRead()) {
        val versionProperties = Properties()
        versionProperties.load(FileInputStream(versionPropertiesFile))
        val name = versionProperties.getProperty("VERSION_NAME")
        val code = versionProperties.getProperty("VERSION_CODE").toInt()
        versionProperties.setProperty("VERSION_CODE", code.toString())
//        versionProperties.store(versionPropertiesFile.writer(), null)
        defaultConfig {
            applicationId = "com.example.androiddevops"
            minSdk = 29
            targetSdk = 35
            versionCode = code
            versionName = name

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables {
                useSupportLibrary = true
            }
        }

        signingConfigs {
            create("release") {
                storeFile = file("../devops_key.jks")
                storePassword = "123456"
                keyAlias = "key"
                keyPassword = "123456"
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.getByName("release")
            }
        }
    } else {
        throw GradleException("version.properties file not found")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun increaseVersionCode() {
    gradle.taskGraph.whenReady {
        if (gradle.taskGraph.hasTask("assembleRelease")) {
            val versionPropertiesFile = file("version.properties")
            if (versionPropertiesFile.canRead()) {
                val versionProperties = Properties()
                versionProperties.load(FileInputStream(versionPropertiesFile))
                val build = versionProperties.getProperty("VERSION_BUILD").toInt().plus(1)
                val code = versionProperties.getProperty("VERSION_CODE").toInt().plus(1)
                versionProperties.setProperty("VERSION_BUILD", build.toString())
                versionProperties.setProperty("VERSION_CODE", code.toString())
                versionProperties.store(versionPropertiesFile.writer(), null)
            }else{
                throw GradleException("version.properties file not found")
            }
        }
    }
}
tasks.register("doIncrementVersionCode"){
    increaseVersionCode()
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline =
        file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}