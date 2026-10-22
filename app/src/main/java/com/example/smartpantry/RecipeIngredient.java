package com.example.smartpantry;

public class RecipeIngredient {
    public String name;
    public double quantity;
    public String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name; this.quantity = quantity; this.unit = unit;
    }
}
