# ProGuard rules for Shopt - Focus on performance optimization
# This is an open source app, so no need for obfuscation

# Keep line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable

# Keep all application classes since this is open source
# This allows R8 to still optimize and remove unused code/methods
-keep class eu.domob.shopt2.** { *; }

# Keep standard Android framework classes that might be referenced dynamically
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep View constructors for XML inflation
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep RecyclerView classes
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder
-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter

# Keep Parcelable CREATOR fields
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Performance: Allow aggressive optimization
-allowaccessmodification
-dontpreverify
