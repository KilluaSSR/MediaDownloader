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

# 通用的 Android 规则 (新项目中通常默认包含)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep class android.support.** { *; }
-keep class androidx.** { *; }
-keep class com.google.** { *; }
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Hilt 规则
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keepnames class **_HiltModules { *; }
-keepnames class **_Provide* { *; }
-keepnames class **_MembersInjector { *; }
-keepnames class **_GeneratedInjector { *; }
-keepnames class *Hilt_* { *; }
-keepnames class *.Hilt_* { *; }
-keepnames class *.*_GeneratedInjector* { *; }
-keep @dagger.hilt.android.AndroidEntryPoint public class *
-keep @dagger.hilt.InstallIn public class *
-keep class dagger.hilt.android.components.** { *; }
-keep,allowobfuscation,allowshrinking @dagger.hilt.EntryPoint interface *
-keep class *Factory { *; }
-keepnames class *Factory { *; }

# Jetpack Compose 规则
-keep class androidx.compose.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.reflect.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.fragment.** { *; }
-keep class androidx.savedstate.** { *; }
-keep class androidx.compose.runtime.internal.** { *; }
-keep class androidx.compose.ui.platform.ComposeView
-keep class androidx.compose.ui.node.** { *; }
-keep class androidx.compose.ui.layout.** { *; }
-keep class androidx.compose.ui.graphics.** { *; }
-keep class androidx.compose.ui.input.** { *; }
-keep class androidx.compose.ui.text.** { *; }
-keep class androidx.compose.ui.unit.** { *; }
-keep class androidx.compose.animation.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.tooling.** { *; }


# 保留你的 Hilt 注入的类及其字段/方法 (如果需要)
-keep @dagger.hilt.android.scopes.* public class killua.dev.mediadownloader.di.** { *; }
-keep @dagger.hilt.android.components.* public class killua.dev.mediadownloader.di.** { *; }
-keep class killua.dev.mediadownloader.viewmodel.**  { *; }
-keep class killua.dev.mediadownloader.ui.** { *; }

-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn javax.lang.model.element.Modifier
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE