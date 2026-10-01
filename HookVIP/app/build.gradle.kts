plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hook.vip"
    // libxposed:service 102.0.0 要求 compileSdk >= 37
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hook.vip"
        minSdk = 28
        targetSdk = 35
        versionCode = 21
        versionName = "1.11"

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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        abortOnError = false
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.dexkit)

    // 现代 libxposed API（LSPosed API 102）：只编译期引用，不打进 APK
    compileOnly(libs.libxposed.api)
    // 模块 App 通过它向框架查询激活状态 / 作用域 / 正在被 Hook 的进程
    implementation(libs.libxposed.service)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
