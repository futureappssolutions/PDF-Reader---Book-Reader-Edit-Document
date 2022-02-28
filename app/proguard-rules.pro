-ignorewarnings

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep public class com.google.android.gms.ads.**{
   public *;
}


-dontwarn com.google.ads.**

-keep public class com.google.ads.* {*;}


-keep public class com.google.gson.** {
    public protected *;
}

-keep public class com.google.ads.internal.* {*;}
-keep public class com.google.ads.internal.AdWebView.* {*;}
-keep public class com.google.ads.internal.state.AdState {*;}
-keep public class com.google.ads.searchads.* {*;}
-keep public class com.google.ads.util.* {*;}

-keep class com.artifex.mupdf.** {*;}
#-dontshrink
#-dontoptimize
#-keepattributes Annotation
#-optimizationpasses 5
-keep public class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.customview.** {*;}
-keep public class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data.** {*;}
-keep public class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.** {*;}
-keep public class pdfreader.pdfviewer.pdfscanner.documentreader.officetool.utils.** {*;}
#-optimizations !method/removal/parameter
#-keep public class com.google.android.gms.ads.** {
#    public *;
#}
#
#-keep public class com.google.ads.** {
#    public *;
#}
#-repackageclasses 'o'



