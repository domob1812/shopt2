package eu.domob.shopt2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class PreferencesActivity extends AppCompatActivity {

    private MaterialSwitch switchDropTicked;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        
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
        switchDropTicked = findViewById(R.id.switchDropTicked);
        LinearLayout dropTickedPreference = findViewById(R.id.dropTickedPreference);
        
        boolean dropTicked = prefs.getBoolean("drop_ticked_to_bottom", false);
        switchDropTicked.setChecked(dropTicked);
        
        dropTickedPreference.setOnClickListener(v -> {
            switchDropTicked.toggle();
        });
        
        switchDropTicked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("drop_ticked_to_bottom", isChecked).apply();
        });
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
