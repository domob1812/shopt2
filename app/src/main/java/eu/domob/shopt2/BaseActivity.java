package eu.domob.shopt2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
    }

    protected void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        int theme = prefs.getInt("theme", 0);
        int mode;
        switch (theme) {
            case 1:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case 2:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        // Only apply the mode if it actually differs from the current one.
        // Calling setDefaultNightMode with the same value on every onCreate
        // is redundant and, when it changes, triggers an activity recreate
        // that can preserve unwanted focus (and show the soft keyboard).
        int current = AppCompatDelegate.getDefaultNightMode();
        if (current == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            current = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (current != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }
}
