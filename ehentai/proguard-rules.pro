# Rules for when minification is enabled (currently disabled for release builds).

# The source class is discovered by the app via dex scanning; keep it intact.
-keep class eu.kanade.tachiyomi.extension.en.ehentai.Ehentai { *; }

# Injekt — generic type tokens are captured via subclasses of FullTypeReference and
# resolved with reflection at runtime, so the Signature attribute is needed.
-keepattributes Signature
-keep,allowshrinking,allowoptimization,allowobfuscation class * extends uy.kohesive.injekt.api.FullTypeReference

# kotlinx-serialization — runtime keeps required for @Serializable types and their
# generated $serializer companions.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    static ** Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <1>$$serializer { *; }
