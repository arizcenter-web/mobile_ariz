# Reglas de release (minify + shrinkResources activos en :app).

# Stack traces legibles en produccion
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.tecsup.app_ariz.**$$serializer { *; }
-keepclassmembers class com.tecsup.app_ariz.** {
    *** Companion;
}
-keepclasseswithmembers class com.tecsup.app_ariz.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Retrofit / OkHttp ----
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Navegacion tipada: las rutas @Serializable se reflejan por nombre ----
-keep class com.tecsup.app_ariz.**.navigation.** { *; }
