# kotlinx.serialization – zachowaj wygenerowane serializery modeli
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class pl.planer.angielski.data.**$$serializer { *; }
-keepclassmembers class pl.planer.angielski.data.** {
    *** Companion;
}
-keepclasseswithmembers class pl.planer.angielski.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
