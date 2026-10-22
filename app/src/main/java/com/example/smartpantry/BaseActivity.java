package com.example.smartpantry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Shared bottom-navigation wiring for the three top-level screens. */
public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav(final int selectedId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;
            Class<?> target;
            if (id == R.id.nav_pantry) target = MainActivity.class;
            else if (id == R.id.nav_suggested) target = SuggestedRecipesActivity.class;
            else target = SettingsActivity.class;
            Intent i = new Intent(this, target);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            if (!(this instanceof MainActivity)) finish();
            return true;
        });
        nav.setSelectedItemId(selectedId);
    }
}
