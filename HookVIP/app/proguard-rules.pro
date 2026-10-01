# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

#######################################
# libxposed 入口
#######################################
# META-INF/xposed/java_init.list 里用字符串引用入口类，模块自身的类一律不能混淆/裁剪
-keep class com.hook.vip.** { *; }

# libxposed service（框架通过 ContentProvider / Binder 反射调用）
-keep class io.github.libxposed.** { *; }
-dontwarn io.github.libxposed.**

#######################################
# DexKit
#######################################
-keep class org.luckypray.dexkit.** { *; }
-dontwarn org.luckypray.dexkit.**

#######################################
# 常用设置
#######################################
# 保留注解、泛型与内部类信息（反射/Json 反序列化需要）
-keepattributes *Annotation*,Signature,InnerClasses
# 保留源码行号，方便 log 定位
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
