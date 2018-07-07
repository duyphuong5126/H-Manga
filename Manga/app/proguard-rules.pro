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
-dontwarn android.arch.persistence.room.**
-dontwarn com.google.auto.common.**
-dontwarn com.google.common.**
-dontwarn com.squareup.javapoet.**
-dontwarn me.eugeniomarletti.kotlin.**
-dontwarn org.abego.treelayout.**
-dontwarn org.antlr.v4.gui.**
-dontwarn org.stringtemplate.v4.gui.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.antlr.runtime.**
-dontwarn retrofit2.**
-dontwarn Type_mirror_extKt
-dontwarn Type_mirror_extKt$WhenMappings
