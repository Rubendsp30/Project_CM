package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.MealSchedule;
import com.example.project_cm.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class MealScheduleAdapter extends RecyclerView.Adapter<MealScheduleAdapter.MealScheduleViewHolder> {

    List<MealSchedule> mealScheduleList;

    public MealScheduleAdapter(List<MealSchedule> mealScheduleList) {
        this.mealScheduleList = mealScheduleList;
    }

    @NonNull
    @Override
    public MealScheduleAdapter.MealScheduleViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_meal_schedule_item, parent, false);
        return new MealScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  MealScheduleViewHolder holder, int position) {
        MealSchedule meal = mealScheduleList.get(position);

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

        public MealScheduleViewHolder(@NonNull View homeMealView) {
            super(homeMealView);
            // Initialize the TextViews for the title and body of the note.
            this.homeMealScheduleHour = homeMealView.findViewById(R.id.homeMealScheduleHour);
            this.homeMealScheduleDate = homeMealView.findViewById(R.id.homeMealScheduleDate);
        }
    }

    @Override
    public int getItemCount() {

        if (mealScheduleList != null) {
            return mealScheduleList.size();
        } else {
            return 0;
        }
    }
}