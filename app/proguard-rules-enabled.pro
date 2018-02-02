# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\dev\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

#-dontshrink
#-dontoptimize
-dontpreverify

-optimizationpasses 5

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-dontwarn org.bouncycastle.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn retrofit2.**

-keep class com.github.** {*;}
-keep class com.simplecityapps.** {*;}
-keep class de.psdev.** {*;}
-keep class me.dm7.** {*;}
-keep class com.heinrichreimersoftware.** {*;}
-keep class com.google.** {*;}
-keep class com.hudomju.** {*;}
-keep class com.knowm.** {*;}
-keep class com.simplecityapps.** {*;}
#-keep class com.jakewharton.** {*;}
-keep class jota.** {*;}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** w(...);
    public static *** e(...);
}

# Proguard configuration for ButterKnife
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keep class **$$ViewBinder { *; }
-keep class **$ViewHolder { *; }
-keep class butterknife.**$Finder { *; }
-keep class **_ViewBinding { *; }
# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}