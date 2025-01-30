   -keepattributes Signature
   -keepattributes *Annotation*

    -keep public class com.google.android.gms.ads.**{
       public *;
    }

    -keep public class com.google.ads.** {
         public *;
    }

    -keep class com.google.ads.** # Don't proguard AdMob classes
    # firebase
    -keep class com.firebase.** { *; }
    -keep class org.apache.** { *; }
    -keepnames class com.fasterxml.jackson.** { *; }
    -keepnames class javax.servlet.** { *; }
    -keepnames class org.ietf.jgss.** { *; }
    -dontwarn org.w3c.dom.**
    -dontwarn org.joda.time.**
    -dontwarn org.shaded.apache.**
    -dontwarn org.ietf.jgss.**

    -keep class com.google.android.gms.** { *; }

    #glide
    -keep public class * implements com.bumptech.glide.module.GlideModule
    -keep public class * extends com.bumptech.glide.module.AppGlideModule
    -keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
      **[] $VALUES;
      public *;
    }

    ##---------------Begin: proguard configuration for Gson ----------
    # Gson uses generic type information stored in a class file when working with
    #fields. Proguard removes such information by default, so configure it to keep
    #all of it.

    # Gson specific classes
    -keep class sun.misc.Unsafe { *; }
    #-keep class com.google.gson.stream.** { *; }

    # Application classes that will be serialized/deserialized over Gson
    -keep class com.google.gson.examples.android.model.** { *; }

    -keep public class org.apache.commons.io.**
    -keep public class com.google.gson.**
    -keep public class com.google.gson.** {public private protected *;}

    ##---------------End: proguard configuration for Gson ----------

