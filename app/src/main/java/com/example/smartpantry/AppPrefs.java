package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {
    private static final String FILE = "settings";
    public static final String EXPIRY_ALERTS = "expiry_alerts";
    public static final String SHOW_ALMOST = "show_almost";

    public static SharedPreferences get(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
