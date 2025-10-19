package com.example.kc_weight_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kc_weight_tracker.repository.WeightsRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * WeightsAdapter is a class that displays the weight history in a RecyclerView.
 */
public class WeightsAdapter extends RecyclerView.Adapter<WeightsAdapter.VH> {

    public interface OnDelete {
        void delete(long id);
    }

    private final OnDelete onDelete;
    private final List<WeightsRepository.WeightDTO> items = new ArrayList<>();

    public WeightsAdapter(List<WeightsRepository.WeightDTO> initial, OnDelete onDelete) {
        if (initial != null) items.addAll(initial);
        this.onDelete = onDelete;
    }

    public void replace(List<WeightsRepository.WeightDTO> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    //* Create the view holder */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_entry, parent, false);
        return new VH(v);
    }

    //* Bind the view holder */
    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        WeightsRepository.WeightDTO row = items.get(pos);

        // Show friendly date, fall back to raw if parsing ever fails
        String pretty = row.dateIso;
        try {
            pretty = LocalDate.parse(row.dateIso)
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception ignore) {}

        h.tvDate.setText(pretty);
        h.tvWeight.setText(String.format("%.1f lb", row.weightLb));
        h.btnDelete.setOnClickListener(v -> onDelete.delete(row.id));
    }

    //* Get the item count */
    @Override
    public int getItemCount() {
        return items.size();
    }

    //* View holder */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvDate, tvWeight;
        final ImageButton btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvWeight = v.findViewById(R.id.tvWeight);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
