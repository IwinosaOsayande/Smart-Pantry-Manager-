package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Runs the strict-matching rule and lists only recipes the user can cook right now. */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private PantryDbHelper db;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");

        db = new PantryDbHelper(this);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        rv.setAdapter(adapter);
        setupBottomNav(R.id.nav_suggested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        List<IngredientMatcher.MatchResult> all =
                IngredientMatcher.evaluate(db.getAllRecipes(), db.getAllItems());

        List<IngredientMatcher.MatchResult> strict = new ArrayList<>();
        List<IngredientMatcher.MatchResult> almost = new ArrayList<>();
        for (IngredientMatcher.MatchResult m : all) {
            if (m.isStrict()) strict.add(m);
            else if (m.missing.size() == 1) almost.add(m);
        }

        adapter.clear();
        adapter.addHeader("Ready to cook (" + strict.size() + ")");
        if (strict.isEmpty()) {
            adapter.addHeader("No recipes match your pantry yet - add more ingredients.");
        } else {
            for (IngredientMatcher.MatchResult m : strict) adapter.addRecipe(m);
        }

        // Optional stretch: clearly separated "Almost There" section (never mixed into strict list)
        if (AppPrefs.get(this).getBoolean(AppPrefs.SHOW_ALMOST, true) && !almost.isEmpty()) {
            adapter.addHeader("Almost there - missing 1 ingredient (" + almost.size() + ")");
            for (IngredientMatcher.MatchResult m : almost) adapter.addRecipe(m);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRecipeClick(Recipe r) {
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, r.id);
        startActivity(i);
    }
}
