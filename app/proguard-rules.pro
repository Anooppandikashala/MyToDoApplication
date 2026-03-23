# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep data models used by SQLite cursor mapping
-keep class com.anoop.myprojects.todoapp.DataModels.** { *; }

# Keep database helper (accessed via try-with-resources / SQLiteOpenHelper)
-keep class com.anoop.myprojects.todoapp.Database.** { *; }

# AdMob / GMS Ads (consumer rules are included in the AAR, these are extra guards)
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Play Core in-app updates
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# ViewBinding classes generated at compile time
-keep class com.anoop.myprojects.todoapp.databinding.** { *; }

# Suppress warnings for optional annotations used by the AndroidX libraries
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**
