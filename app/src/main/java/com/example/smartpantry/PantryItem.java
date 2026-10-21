package com.example.smartpantry;

public class PantryItem {
    public long id;
    public String name;
    public double quantity;
    public String unit;
    public String expiry; // yyyy-MM-dd or null

    public PantryItem() {}

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id; this.name = name; this.quantity = quantity; this.unit = unit; this.expiry = expiry;
    }
}
