// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // AGP 9.x：libxposed:service 102 需要 compileSdk 37，AGP 8.x 读不了 android-37.0
    // 9.4.1 = 当前 Android Studio 支持的最高版本
    alias(libs.plugins.android.application) apply false
}
