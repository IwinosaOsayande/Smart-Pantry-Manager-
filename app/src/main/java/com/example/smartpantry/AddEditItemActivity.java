package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/** Add / Edit ingredient form with input validation. */
public class AddEditItemActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "item_id";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private PantryDbHelper db;
    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new PantryDbHelper(this);
        etName = findViewById(R.id.et_name);
        etQty = findViewById(R.id.et_qty);
        etExpiry = findViewById(R.id.et_expiry);
        spUnit = findViewById(R.id.sp_unit);
        Button save = findViewById(R.id.btn_save);
        Button clear = findViewById(R.id.btn_clear_expiry);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit ingredient");
            PantryItem it = db.getItem(itemId);
            if (it != null) {
                etName.setText(it.name);
                etQty.setText(PantryAdapter.formatQty(it.quantity));
                etExpiry.setText(it.expiry == null ? "" : it.expiry);
                for (int i = 0; i < UNITS.length; i++) if (UNITS[i].equalsIgnoreCase(it.unit)) spUnit.setSelection(i);
            }
        } else {
            setTitle("Add ingredient");
        }

        etExpiry.setOnClickListener(v -> pickDate());
        clear.setOnClickListener(v -> etExpiry.setText(""));
        save.setOnClickListener(v -> save());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        boolean ok = true;

        if (name.isEmpty()) { etName.setError("Name is required"); ok = false; }
        else if (name.length() > 40) { etName.setError("Max 40 characters"); ok = false; }

        double qty = 0;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { etQty.setError("Must be greater than 0"); ok = false; }
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number"); ok = false;
        }
        if (!ok) return;

        String expiry = etExpiry.getText().toString().trim();
        PantryItem item = new PantryItem(itemId, name, qty, (String) spUnit.getSelectedItem(),
                expiry.isEmpty() ? null : expiry);

        if (itemId == -1) db.insertItem(item); else db.updateItem(item);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
