package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Two view types: section header / message rows and recipe rows. */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener { void onRecipeClick(Recipe r); }

    private static final int HEADER = 0, RECIPE = 1;

    private static class Row {
        String text;
        IngredientMatcher.MatchResult result;
    }

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener l) { listener = l; }

    public void clear() { rows.clear(); }

    public void addHeader(String text) {
        Row r = new Row(); r.text = text; rows.add(r);
    }

    public void addRecipe(IngredientMatcher.MatchResult m) {
        Row r = new Row(); r.result = m; rows.add(r);
    }

    @Override public int getItemViewType(int pos) { return rows.get(pos).result == null ? HEADER : RECIPE; }
    @Override public int getItemCount() { return rows.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int type) {
        LayoutInflater inf = LayoutInflater.from(p.getContext());
        if (type == HEADER) return new HeaderVH(inf.inflate(R.layout.item_header, p, false));
        return new RecipeVH(inf.inflate(R.layout.item_recipe, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        Row row = rows.get(pos);
        if (h instanceof HeaderVH) {
            ((HeaderVH) h).text.setText(row.text);
            return;
        }
        final IngredientMatcher.MatchResult m = row.result;
        RecipeVH vh = (RecipeVH) h;
        vh.name.setText(m.recipe.name);
        vh.sub.setText(m.isStrict()
                ? m.recipe.ingredients.size() + " ingredients - you have everything"
                : "Missing: " + android.text.TextUtils.join(", ", m.missing));
        vh.itemView.setOnClickListener(v -> listener.onRecipeClick(m.recipe));
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView text;
        HeaderVH(View v) { super(v); text = v.findViewById(R.id.tv_header); }
    }

    static class RecipeVH extends RecyclerView.ViewHolder {
        TextView name, sub;
        RecipeVH(View v) { super(v); name = v.findViewById(R.id.tv_recipe_name); sub = v.findViewById(R.id.tv_recipe_sub); }
    }
}
