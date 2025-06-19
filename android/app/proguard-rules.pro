# Keep only the minimal Flutter embedding classes actually in the manifest
-keep class io.flutter.embedding.android.FlutterApplication { *; }
-keep class io.flutter.embedding.android.FlutterActivity { *; }
# Keep GeneratedPluginRegistrant (auto-registration of plugins)
-keep class io.flutter.plugins.GeneratedPluginRegistrant { *; }

# Keep Dart entrypoints
-keep class * {
    public static void main(java.lang.String[]);
}

# Suppress warnings for missing Play Core classes (deferred components)
-dontwarn com.google.android.play.core.splitcompat.SplitCompatApplication
-dontwarn com.google.android.play.core.splitinstall.SplitInstallException
-dontwarn com.google.android.play.core.splitinstall.SplitInstallManager
-dontwarn com.google.android.play.core.splitinstall.SplitInstallManagerFactory
-dontwarn com.google.android.play.core.splitinstall.SplitInstallRequest$Builder
-dontwarn com.google.android.play.core.splitinstall.SplitInstallRequest
-dontwarn com.google.android.play.core.splitinstall.SplitInstallSessionState
-dontwarn com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
-dontwarn com.google.android.play.core.tasks.OnFailureListener
-dontwarn com.google.android.play.core.tasks.OnSuccessListener
-dontwarn com.google.android.play.core.tasks.Task
