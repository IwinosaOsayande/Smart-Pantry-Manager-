package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean expiryAlerts = true;

    public PantryAdapter(Listener l) { listener = l; }

    public void setItems(List<PantryItem> newItems, boolean expiryAlerts) {
        items.clear();
        items.addAll(newItems);
        this.expiryAlerts = expiryAlerts;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final PantryItem it = items.get(position);
        h.name.setText(it.name);
        h.qty.setText(formatQty(it.quantity) + " " + it.unit);
        h.expiry.setVisibility(View.GONE);

        if (it.expiry != null && !it.expiry.isEmpty()) {
            h.expiry.setVisibility(View.VISIBLE);
            h.expiry.setText("Expires: " + it.expiry);
            h.expiry.setTextColor(Color.GRAY);
            if (expiryAlerts) {
                long days = daysUntil(it.expiry);
                if (days < 0) { h.expiry.setText("EXPIRED: " + it.expiry); h.expiry.setTextColor(Color.RED); }
                else if (days <= 3) { h.expiry.setText("Expires soon: " + it.expiry); h.expiry.setTextColor(Color.rgb(230, 120, 0)); }
            }
        }
        h.itemView.setOnClickListener(v -> listener.onEdit(it));
        h.delete.setOnClickListener(v -> listener.onDelete(it));
    }

    @Override public int getItemCount() { return items.size(); }

    static String formatQty(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    private static long daysUntil(String yyyyMMdd) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(yyyyMMdd);
            long today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .parse(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date())).getTime();
            return TimeUnit.MILLISECONDS.toDays(d.getTime() - today);
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        ImageButton delete;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            qty = v.findViewById(R.id.tv_qty);
            expiry = v.findViewById(R.id.tv_expiry);
            delete = v.findViewById(R.id.btn_delete);
        }
    }
}
