package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Pantry list screen (Read + entry point for Create / Update / Delete). */
public class MainActivity extends BaseActivity implements PantryAdapter.Listener {

    private PantryDbHelper db;
    private PantryAdapter adapter;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Pantry");

        db = new PantryDbHelper(this);
        empty = findViewById(R.id.tv_empty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> items = db.getAllItems();
        boolean alerts = AppPrefs.get(this).getBoolean(AppPrefs.EXPIRY_ALERTS, true);
        adapter.setItems(items, alerts);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent i = new Intent(this, AddEditItemActivity.class);
        i.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.id);
        startActivity(i);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.name + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteItem(item.id);
                    Toast.makeText(this, item.name + " deleted", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
