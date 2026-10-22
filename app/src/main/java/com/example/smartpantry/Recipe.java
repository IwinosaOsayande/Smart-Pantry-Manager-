package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public long id;
    public String name;
    public String steps;
    public List<RecipeIngredient> ingredients = new ArrayList<>();
}
