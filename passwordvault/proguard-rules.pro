# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Andy\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

##------------------- Begin GateKeeperSDK proguard config ----------------

# keep all native code classes
-keep class java.awt.** { *; }
-keep class com.sun.jna.** { *; }
-keep class com.neurotec.** { *; }
-keep class android.support.** { *; }
# Gson specific classes
-keep interface com.sun.jna.** { *; }
-keep interface android.support.** { *; }

-keep class com.google.**

# (2)Simple XML
-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }

-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn com.neurotec.**
-dontwarn com.google.**
-dontwarn org.slf4j.**
-dontwarn sun.misc.Unsafe

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
