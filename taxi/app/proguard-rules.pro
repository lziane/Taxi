# --- OPTIMISATIONS GÉNÉRALES ---
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# --- SÉCURITÉ & OBFUSCATION ---
# Protège les noms de méthodes et attributs de tes services
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses

# --- JSOUP (Important pour ton scraping) ---
-keep class org.jsoup.** { *; }

# --- RETROFIT & OKHTTP ---
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okio.**
-dontwarn javax.annotation.**

# --- KOTLINX SERIALIZATION ---
-keepclassmembernames class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}

# --- NETTOYAGE DES LOGS ---
# Supprime les logs de debug en production (R8)
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
