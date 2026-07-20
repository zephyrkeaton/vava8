# Keep serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class com.vava8.app.data.model.** {
    *** Companion;
}
-keep.class com.vava8.app.data.model.** { *; }
