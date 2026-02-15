# ============================================================
# Fahh App — ProGuard/R8 Rules
# ============================================================

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**

# --- Room ---
-keep class com.fahh.data.database.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# --- CameraX ---
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- AdMob / Google Play Services ---
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# --- Compose ---
-dontwarn androidx.compose.**

# --- Kotlin Coroutines ---
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# --- Data model (used with Room) ---
-keep class com.fahh.data.model.** { *; }

# --- Keep app entry points ---
-keep class com.fahh.MainActivity { *; }
-keep class com.fahh.FahhApplication { *; }

# --- Guava (CameraX dependency) ---
-dontwarn com.google.common.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**

# --- General Android ---
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
