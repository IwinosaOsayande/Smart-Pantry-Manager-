package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic: strict recipe matching.
 *
 * A recipe is "strict" only if EVERY ingredient is in the pantry in at least the required
 * quantity. Names are normalised (case, whitespace, simple plurals) and quantities are
 * converted to a common base unit (g / ml / count) so 1 kg covers 500 g, 0.5 l covers 300 ml.
 */
public class IngredientMatcher {

    public static class MatchResult {
        public final Recipe recipe;
        public final List<String> missing = new ArrayList<>();
        MatchResult(Recipe r) { recipe = r; }
        public boolean isStrict() { return missing.isEmpty(); }
    }

    private static class Qty {
        final String dim;   // "mass", "volume", "count" or "u:<unit>" for unrecognised units
        final double base;
        Qty(String dim, double base) { this.dim = dim; this.base = base; }
    }

    /** Evaluates every recipe against the pantry. Callers split by missing.size(). */
    public static List<MatchResult> evaluate(List<Recipe> recipes, List<PantryItem> pantry) {
        // normalised name -> (dimension -> total base quantity)
        Map<String, Map<String, Double>> totals = new HashMap<>();
        for (PantryItem p : pantry) {
            Qty q = toBase(p.quantity, p.unit);
            String key = normalizeName(p.name);
            Map<String, Double> byDim = totals.get(key);
            if (byDim == null) { byDim = new HashMap<>(); totals.put(key, byDim); }
            Double cur = byDim.get(q.dim);
            byDim.put(q.dim, (cur == null ? 0 : cur) + q.base);
        }

        List<MatchResult> results = new ArrayList<>();
        for (Recipe r : recipes) {
            MatchResult m = new MatchResult(r);
            for (RecipeIngredient ri : r.ingredients) {
                Qty need = toBase(ri.quantity, ri.unit);
                Map<String, Double> byDim = totals.get(normalizeName(ri.name));
                Double have = byDim == null ? null : byDim.get(need.dim);
                if (have == null || have + 1e-9 < need.base) m.missing.add(ri.name);
            }
            results.add(m);
        }
        return results;
    }

    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ").trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return s;
        int idx = s.lastIndexOf(' ');
        String head = idx < 0 ? "" : s.substring(0, idx + 1);
        return head + singular(idx < 0 ? s : s.substring(idx + 1));
    }

    private static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("sses") || w.endsWith("xes")) return w.substring(0, w.length() - 2);
        if (w.endsWith("ss")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);
        return w;
    }

    private static Qty toBase(double q, String unitRaw) {
        String u = unitRaw == null ? "" : unitRaw.toLowerCase(Locale.ROOT).trim().replace(".", "");
        switch (u) {
            case "g": case "gram": case "grams": return new Qty("mass", q);
            case "kg": case "kilogram": case "kilograms": return new Qty("mass", q * 1000);
            case "mg": return new Qty("mass", q / 1000);
            case "oz": return new Qty("mass", q * 28.35);
            case "lb": case "lbs": return new Qty("mass", q * 453.6);
            case "ml": case "millilitre": case "milliliter": return new Qty("volume", q);
            case "l": case "litre": case "liter": case "litres": case "liters": return new Qty("volume", q * 1000);
            case "tsp": case "teaspoon": return new Qty("volume", q * 5);
            case "tbsp": case "tablespoon": return new Qty("volume", q * 15);
            case "cup": case "cups": return new Qty("volume", q * 240);
            case "": case "pcs": case "pc": case "piece": case "pieces": case "whole":
            case "unit": case "units": case "clove": case "cloves": case "slice": case "slices":
            case "can": case "tin":
                return new Qty("count", q);
            default: return new Qty("u:" + u, q);
        }
    }
}
