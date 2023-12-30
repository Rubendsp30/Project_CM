package com.example.project_cm.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.R;

import java.util.List;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder> {

    private final List<VaccineEntity> vaccineList;

    public VaccineAdapter(List<VaccineEntity> vaccineList) {
        this.vaccineList = vaccineList;
    }

    @NonNull
    @Override
    public VaccineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vaccine, parent, false);
        return new VaccineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineViewHolder holder, int position) {
        VaccineEntity vaccine = vaccineList.get(position);
        holder.textViewVaccineName.setText(vaccine.getVaccineName());
        holder.textViewVaccineDate.setText(vaccine.getVaccineDate());


        int daysLeft = vaccine.getDaysLeft();

        if (daysLeft <= 5) {
            // Menos de 5 dias: vrmelho
            holder.textViewDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_300));
        } else if (daysLeft <= 10) {
            // Entre 6 e 10 dias: laranja
            holder.textViewDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_300));
        } else {
            // Mais de 10 dias: verde
            holder.textViewDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_400));
        }

        // Atualizar o texto dos dias restantes
        String daysLeftText = daysLeft + " days left";

        holder.textViewDaysLeft.setText(daysLeftText);
    }

    @Override
    public int getItemCount() {
        return vaccineList.size();
    }

    public static class VaccineViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewVaccineName;
        public final TextView textViewVaccineDate;
        public final TextView textViewDaysLeft;

        VaccineViewHolder(View itemView) {
            super(itemView);
            this.textViewVaccineName = itemView.findViewById(R.id.textViewVaccineName);
            this.textViewVaccineDate = itemView.findViewById(R.id.textViewVaccineDate);
            this.textViewDaysLeft = itemView.findViewById(R.id.textViewDaysLeft);
        }
    }
}