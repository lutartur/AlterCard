# --- Gson ---
# Keep annotation attributes for Gson TypeToken and @SerializedName
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Keep data classes used for Gson serialization (Drive sync)
-keep class com.altercard.Card { *; }
-keep class com.altercard.DrivePayload { *; }

# Gson internals
-keep class com.google.gson.stream.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# --- ZXing ---
# BarcodeFormat values are referenced by name via .name / BarcodeFormat.valueOf()
-keepnames enum com.google.zxing.BarcodeFormat

# --- Google API Client / Drive ---
-dontwarn com.google.api.client.**
-dontwarn com.google.auth.**
-dontwarn sun.misc.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.**
