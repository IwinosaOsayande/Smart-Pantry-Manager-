package com.example.smartpantry;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Recipe r = new PantryDbHelper(this).getRecipe(getIntent().getLongExtra(EXTRA_RECIPE_ID, -1));
        if (r == null) { finish(); return; }

        setTitle(r.name);
        ((TextView) findViewById(R.id.tv_title)).setText(r.name);

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient i : r.ingredients) {
            sb.append("\u2022 ").append(PantryAdapter.formatQty(i.quantity)).append(" ")
              .append(i.unit).append("  ").append(i.name).append("\n");
        }
        ((TextView) findViewById(R.id.tv_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tv_steps)).setText(r.steps);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
