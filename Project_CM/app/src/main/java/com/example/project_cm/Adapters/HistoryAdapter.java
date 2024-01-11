package com.example.project_cm.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.History;
import com.example.project_cm.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<History> historyList;

    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_history, parent, false);
        return new HistoryAdapter.HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        History history = historyList.get(position);
        holder.textViewQuantityFood.setText(String.valueOf(history.getQuantityServed() + "g"));

        Date date = history.getMealTime();
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d MMMM '('HH'h'mm)", Locale.getDefault());
            String formattedDate = formatter.format(date);
            holder.textViewDateFood.setText(formattedDate);
        } else {
            holder.textViewDateFood.setText("Date not available");
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<History> newList) {
        this.historyList = newList;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewDateFood;
        public final TextView textViewQuantityFood;

        HistoryViewHolder(View itemView) {
            super(itemView);
            this.textViewDateFood = itemView.findViewById(R.id.textViewDateFood);
            this.textViewQuantityFood = itemView.findViewById(R.id.textViewQuantityFood);
        }
    }

}
