package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        final SharedPreferences prefs = AppPrefs.get(this);
        SwitchMaterial expiry = findViewById(R.id.sw_expiry);
        SwitchMaterial almost = findViewById(R.id.sw_almost);

        expiry.setChecked(prefs.getBoolean(AppPrefs.EXPIRY_ALERTS, true));
        almost.setChecked(prefs.getBoolean(AppPrefs.SHOW_ALMOST, true));

        expiry.setOnCheckedChangeListener((CompoundButton b, boolean on) ->
                prefs.edit().putBoolean(AppPrefs.EXPIRY_ALERTS, on).apply());
        almost.setOnCheckedChangeListener((CompoundButton b, boolean on) ->
                prefs.edit().putBoolean(AppPrefs.SHOW_ALMOST, on).apply());

        setupBottomNav(R.id.nav_settings);
    }
}
