package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite storage: pantry (CRUD) plus seeded recipes (read). */
public class PantryDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 1;

    public PantryDbHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------------- Pantry CRUD ----------------
    public long insertItem(PantryItem i) {
        return getWritableDatabase().insert("pantry", null, toValues(i));
    }

    public int updateItem(PantryItem i) {
        return getWritableDatabase().update("pantry", toValues(i), "id=?", new String[]{String.valueOf(i.id)});
    }

    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().query("pantry", null, "id=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    private ContentValues toValues(PantryItem i) {
        ContentValues v = new ContentValues();
        v.put("name", i.name);
        v.put("quantity", i.quantity);
        v.put("unit", i.unit);
        v.put("expiry", i.expiry);
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ---------------- Recipes (read) ----------------
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, null, null, null, null, "name")) {
            while (c.moveToNext()) list.add(recipeFrom(db, c));
        }
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? recipeFrom(db, c) : null;
        }
    }

    private Recipe recipeFrom(SQLiteDatabase db, Cursor c) {
        Recipe r = new Recipe();
        r.id = c.getLong(c.getColumnIndexOrThrow("id"));
        r.name = c.getString(c.getColumnIndexOrThrow("name"));
        r.steps = c.getString(c.getColumnIndexOrThrow("steps"));
        try (Cursor ic = db.query("recipe_ingredients", null, "recipe_id=?",
                new String[]{String.valueOf(r.id)}, null, null, "id")) {
            while (ic.moveToNext()) {
                r.ingredients.add(new RecipeIngredient(
                        ic.getString(ic.getColumnIndexOrThrow("name")),
                        ic.getDouble(ic.getColumnIndexOrThrow("quantity")),
                        ic.getString(ic.getColumnIndexOrThrow("unit"))));
            }
        }
        return r;
    }

    // ---------------- Seed data (runs once, on first install) ----------------
    // Ingredient format: "name|quantity|unit"
    private void seed(SQLiteDatabase db) {
        add(db, "Scrambled Eggs", "1. Whisk the eggs with salt.\n2. Melt butter in a pan on low heat.\n3. Pour in eggs and stir gently until just set.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");
        add(db, "Cheese Omelette", "1. Whisk eggs with salt.\n2. Melt butter in a pan and add eggs.\n3. When almost set, add grated cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");
        add(db, "Tomato Pasta", "1. Boil pasta until tender.\n2. Fry chopped onion and garlic in olive oil.\n3. Add chopped tomatoes and salt, simmer 15 minutes.\n4. Toss with drained pasta.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|2|tbsp", "salt|1|tsp");
        add(db, "Egg Fried Rice", "1. Heat oil and fry chopped onion.\n2. Add cooked rice and soy sauce.\n3. Push rice aside, scramble eggs, then mix together.",
                "rice|300|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        add(db, "Pancakes", "1. Mix flour, sugar, milk and egg into a smooth batter.\n2. Melt a little butter in a pan.\n3. Cook ladles of batter until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|2|tbsp", "butter|20|g");
        add(db, "Garlic Butter Toast", "1. Mash crushed garlic into soft butter.\n2. Spread on bread.\n3. Grill or toast until golden.",
                "bread|4|pcs", "butter|40|g", "garlic|2|pcs");
        add(db, "Cheese Toastie", "1. Butter the outside of the bread.\n2. Place cheese between slices.\n3. Fry on both sides until golden and melted.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");
        add(db, "Mashed Potatoes", "1. Peel and boil potatoes until soft.\n2. Drain and mash with butter, warm milk and salt.",
                "potato|500|g", "butter|30|g", "milk|100|ml", "salt|1|tsp");
        add(db, "Chicken Stir-Fry", "1. Slice chicken, onion and carrots.\n2. Fry chicken in hot oil until cooked.\n3. Add vegetables and soy sauce, stir-fry 5 minutes.",
                "chicken|300|g", "onion|1|pcs", "carrot|2|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        add(db, "Tomato Soup", "1. Fry onion and garlic.\n2. Add chopped tomatoes, stock and salt.\n3. Simmer 20 minutes and blend until smooth.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "vegetable stock|500|ml", "salt|1|tsp");
        add(db, "Banana Smoothie", "1. Peel the bananas.\n2. Blend with milk and honey until smooth.",
                "banana|2|pcs", "milk|250|ml", "honey|1|tbsp");
        add(db, "Peanut Butter Sandwich", "1. Spread peanut butter on a slice of bread.\n2. Top with the second slice.",
                "bread|2|pcs", "peanut butter|2|tbsp");
        add(db, "Mac and Cheese", "1. Boil macaroni until tender and drain.\n2. Warm milk and butter, stir in grated cheese until melted.\n3. Mix with the macaroni.",
                "macaroni|250|g", "cheese|150|g", "milk|200|ml", "butter|20|g");
        add(db, "Veggie Omelette", "1. Dice onion, tomato and pepper.\n2. Fry briefly, then pour in whisked eggs with salt.\n3. Cook until set and fold.",
                "egg|3|pcs", "onion|1|pcs", "tomato|1|pcs", "bell pepper|1|pcs", "salt|1|tsp");
        add(db, "Baked Potatoes", "1. Heat oven to 200C.\n2. Rub potatoes with olive oil and salt.\n3. Bake 50 minutes, split and add butter.",
                "potato|4|pcs", "butter|20|g", "salt|1|tsp", "olive oil|1|tbsp");
        add(db, "Creamy Oatmeal", "1. Heat oats and milk gently, stirring, for 5 minutes.\n2. Sweeten with honey.",
                "oats|80|g", "milk|250|ml", "honey|1|tbsp");
        add(db, "Lentil Curry", "1. Fry onion and garlic in oil.\n2. Add curry powder, lentils, tomatoes and 500 ml water.\n3. Simmer 25 minutes until soft.",
                "lentil|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|2|tsp", "oil|1|tbsp");
        add(db, "French Toast", "1. Whisk eggs, milk and sugar.\n2. Soak bread slices briefly.\n3. Fry in butter until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp", "butter|10|g");
        add(db, "Rice and Beans", "1. Fry chopped onion in oil.\n2. Add rice, beans, salt and enough water to cover.\n3. Simmer until the rice is cooked.",
                "rice|200|g", "beans|250|g", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
        add(db, "Lemon Tuna Pasta", "1. Boil pasta and drain.\n2. Warm garlic in olive oil, add tuna and lemon juice.\n3. Toss with pasta.",
                "pasta|200|g", "tuna|160|g", "garlic|2|pcs", "olive oil|2|tbsp", "lemon|1|pcs");
    }

    private void add(SQLiteDatabase db, String name, String steps, String... ings) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long rid = db.insert("recipes", null, rv);
        for (String s : ings) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", rid);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }
}
