package eu.domob.shopt2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class PreferencesActivity extends BaseActivity {

    private MaterialSwitch switchDropTicked;
    private LinearLayout themePreference;
    private TextView tvThemeValue;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_preferences);
        
        setupToolbar();
        setupPreferences();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupPreferences() {
        themePreference = findViewById(R.id.themePreference);
        tvThemeValue = findViewById(R.id.tvThemeValue);
        switchDropTicked = findViewById(R.id.switchDropTicked);
        LinearLayout dropTickedPreference = findViewById(R.id.dropTickedPreference);
        
        updateThemeValue();
        
        themePreference.setOnClickListener(v -> showThemeDialog());
        
        boolean dropTicked = prefs.getBoolean("drop_ticked_to_bottom", false);
        switchDropTicked.setChecked(dropTicked);
        
        dropTickedPreference.setOnClickListener(v -> {
            switchDropTicked.toggle();
        });
        
        switchDropTicked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("drop_ticked_to_bottom", isChecked).apply();
        });
    }

    private void showThemeDialog() {
        String[] themes = {
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        };
        
        int currentTheme = prefs.getInt("theme", 0);
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                prefs.edit().putInt("theme", which).apply();
                updateThemeValue();
                applyTheme();
                dialog.dismiss();
                recreate();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void updateThemeValue() {
        int theme = prefs.getInt("theme", 0);
        String[] themes = {
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        };
        tvThemeValue.setText(themes[theme]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
