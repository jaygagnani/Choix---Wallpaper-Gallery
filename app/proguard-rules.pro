# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\jay\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#picasso
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-keep interface com.fasterxml.jackson.core.** { *; }
-dontwarn com.fasterxml.jackson.core.**

#-keep class com.amazonaws.auth.** { *; }
-keep class com.amazonaws.auth.AWSCredentials { *; }
-keep class com.amazonaws.auth.AnonymousAWSCredentials { *; }

#-keep class com.amazonaws.services.s3.** { *; }
-keep class com.amazonaws.services.s3.AmazonS3Client { *; }

#-keep class com.amazonaws.services.s3.model.** { *; }
-keep class com.amazonaws.services.s3.model.ObjectListing { *; }
-keep class com.amazonaws.services.s3.model.S3ObjectSummary { *; }

#-keep class com.aurelhubert.ahbottomnavigation.** { *; }
-keep class com.aurelhubert.ahbottomnavigation.AHBottomNavigation { *; }
-keep class com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem { *; }
-keep class com.google.android.gms.ads.** { *; }

-keepattributes *Annotation*


# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
