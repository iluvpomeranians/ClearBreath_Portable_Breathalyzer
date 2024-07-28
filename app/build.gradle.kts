plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.coen390androidproject_breathalyzerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.coen390androidproject_breathalyzerapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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

dependencies {
    implementation("androidx.core:core:1.7.0")
    implementation("co.yml:ycharts:2.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:compose-m2:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:core:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:views:2.0.0-alpha.25")
    implementation(libs.firebase.inappmessaging)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.core:core:1.7.0")
    implementation("co.yml:ycharts:2.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation(platform("androidx.compose:compose-bom:2021.09.01"))
    implementation("androidx.compose.ui:ui:1.0.5")
    implementation("androidx.compose.ui:ui-graphics:1.0.5")
    implementation("androidx.compose.ui:ui-tooling-preview:1.0.5")
    implementation("androidx.compose.material3:material3:1.0.0-alpha01")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:compose-m2:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:core:2.0.0-alpha.25")
    implementation("com.patrykandpatrick.vico:views:2.0.0-alpha.25")
    implementation("com.google.firebase:firebase-inappmessaging:19.1.1")
    implementation("androidx.preference:preference:1.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2021.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.0.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.0.5")
}
