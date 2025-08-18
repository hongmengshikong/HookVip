# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#######################################
# Xposed 入口类 (不要被混淆/删除)
#######################################

# 保留实现了 Xposed 接口的类
#-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage { *; }
#-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit { *; }

# 完整保留单独的 HookEntry 类，
-keep class com.hook.vip.HookEntry {
    *;
}

#######################################
# DexKit 相关
#######################################

# 保留 DexKit 库
#-keep class org.luckypray.dexkit.** { *; }

# 如果你用 DexKit 搜索自己模块里的类
#-keep class com.hook.vip.** { *; }

#######################################
# 反射相关
#######################################

# 保留所有你用 Class.forName()/反射调用的目标类
# （这里举例目标 app 的包名，你需要替换）
#-keep class com.target.app.** { *; }

#######################################
# 常用设置
#######################################

# 保留注解
#-keepattributes *Annotation*

# 保留泛型和内部类信息（防止反射出错）
#-keepattributes Signature,InnerClasses

# （可选）保留源码行号，方便 log 定位
#-keepattributes SourceFile,LineNumberTable
