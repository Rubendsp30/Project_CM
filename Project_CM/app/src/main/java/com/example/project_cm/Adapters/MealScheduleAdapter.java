package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.Device;
import com.example.project_cm.Fragments.TreatPopUpFragment;
import com.example.project_cm.MealSchedule;
import com.example.project_cm.R;
import com.example.project_cm.ViewModels.DeviceViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MealScheduleAdapter extends RecyclerView.Adapter<MealScheduleAdapter.MealScheduleViewHolder> {

    List<MealSchedule> meals;

    public MealScheduleAdapter(List<MealSchedule> meals) {
        this.meals = meals;
    }

    @NonNull
    @Override
    public MealScheduleAdapter.MealScheduleViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_meal_schedule_item, parent, false);
        return new MealScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  MealScheduleViewHolder holder, int position) {
        MealSchedule meal = meals.get(position);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String formattedTime = timeFormat.format(meal.getMealTime());

        holder.homeMealScheduleHour.setText(formattedTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM");
        String formattedDate = dateFormat.format(meal.getMealTime());
        holder.homeMealScheduleDate.setText(formattedDate);
    }

    // ViewHolder class for holding the views of each note card.
    public static class MealScheduleViewHolder extends RecyclerView.ViewHolder {
        public final TextView homeMealScheduleHour;
        public final TextView homeMealScheduleDate;

        public MealScheduleViewHolder(@NonNull View noteView) {
            super(noteView);
            // Initialize the TextViews for the title and body of the note.
            this.homeMealScheduleHour = noteView.findViewById(R.id.homeMealScheduleHour);
            this.homeMealScheduleDate = noteView.findViewById(R.id.homeMealScheduleDate);
        }
    }

    @Override
    public int getItemCount() {

        if (meals != null) {
            return meals.size();
        } else {
            return 0;
        }
    }
}