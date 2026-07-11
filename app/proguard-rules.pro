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




-printmapping rules-mapping.txt
#-obfuscationdictionary ./custom-rules.txt
#-classobfuscationdictionary ./custom-rules.txt
#-packageobfuscationdictionary ./custom-rules.txt
-keep class android.**{*;}
-keep class androidx.**{*;}
-keep class org.** {*;}
-dontusemixedcaseclassnames
-verbose
-optimizationpasses 5
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-renamesourcefileattribute SourceFile

-optimizations !code/simplification/cast,!field/*,!class/merging/*

-keep public class * extends java.lang.Exception


# Retrofit
-keep class retrofit2.**{*;}
-keepattributes Signature
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# okhttp
-dontwarn okio.**
#-dontwarn okhttp3.logging.**
#-keep class okhttp3.internal.**{*;}
#-keep class okhttp3.**{*;}

-keep class com.google.gson.** {*;}
-keep class org.json.** {*;}


#firebase
-keep public class com.google.firebase.** {*;}
-keep class com.google.android.gms.internal.** {*;}
-keepclasseswithmembers class com.google.firebase.FirebaseException

-dontwarn com.google.android.gms.safetynet.SafetyNet
-dontwarn com.google.android.gms.safetynet.SafetyNetApi$AttestationResponse
-dontwarn com.google.android.gms.safetynet.SafetyNetClient
-keep public class com.google.firebase.* {*;}

#solarengine
-keep class com.reyun.** {*; }
-keep class route.**{*;}
-keep interface com.reyun.** {*; }
-keep interface route.**{*;}
-dontwarn com.reyun.**
-dontwarn org.json.**
-keep class org.json.**{*;}

-keep public class com.android.installreferrer.** { *; }

-keep class com.huawei.hms.**{*;}
-keep class com.hihonor.**{*;}


-keep class androidx.media3.** { *; }

-keep class androidx.media3.ui.PlayerView { *; }


#viewbing
-keep class com.chat.jolt.databinding.**{*;}


-keep class io.rong.** {*;}
-keep class cn.rongcloud.** {*;}
-keep class * implements io.rong.imlib.model.MessageContent {*;}
-dontwarn io.rong.push.**
-dontnote com.xiaomi.**
-dontnote com.google.android.gms.gcm.**
-dontnote io.rong.**

-ignorewarnings


-keep class com.luck.picture.lib.** { *; }
-keep class com.luck.lib.camerax.** { *; }
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }


-keep class com.alibaba.sdk.android.oss.** { *; }
-keepclassmembers class com.alibaba.sdk.android.oss.** {
    public <init>(...);
}
-keep class com.alibaba.sdk.android.oss.**$* { *; }
-keep interface com.alibaba.sdk.android.oss.callback.** { *; }
-dontwarn com.alibaba.sdk.android.oss.**
-keep class com.alibaba.sdk.android.oss.common.auth.** { *; }
-keep class com.alibaba.sdk.android.oss.network.** { *; }



-keep class com.reyun.** {*; }
-keep class route.**{*;}
-keep interface com.reyun.** {*; }
-keep interface route.**{*;}
-dontwarn com.reyun.**
-dontwarn org.json.**
-keep class org.json.**{*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
-keep class com.huawei.hms.**{*;}
-keep class com.hihonor.**{*;}


-keep class com.chat.jolt.activity.**{*;}
-keep class com.chat.jolt.adapter.**{*;}
-keep class com.chat.jolt.api.**{*;}
-keep class com.chat.jolt.data.**{*;}
-keep class com.chat.jolt.dialog.**{*;}
-keep class com.chat.jolt.fragment.**{*;}
-keep class com.chat.jolt.helper.**{*;}
-keep class com.chat.jolt.viewmodel.**{*;}
-keep class com.chat.jolt.widget.**{*;}
-keep class com.chat.jolt.manager.**{*;}


-keep class com.chat.lib_common.activity.**{*;}
-keep class com.chat.lib_common.adapter.**{*;}
-keep class com.chat.lib_common.app.**{*;}
-keep class com.chat.lib_common.bus.**{*;}
-keep class com.chat.lib_common.constant.**{*;}
-keep class com.chat.lib_common.data.**{*;}
-keep class com.chat.lib_common.dialog.**{*;}
-keep class com.chat.lib_common.fragment.**{*;}
-keep class com.chat.lib_common.http.**{*;}
-keep class com.chat.lib_common.im.**{*;}
-keep class com.chat.lib_common.manager.**{*;}
-keep class com.chat.lib_common.util.**{*;}
-keep class com.chat.lib_common.viewmodel.**{*;}
-keep class com.chat.lib_common.widget.**{*;}
-keep class com.chat.lib_common.tracking.**{*;}
