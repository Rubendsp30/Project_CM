package com.example.project_cm.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_cm.MealSchedule;
import com.example.project_cm.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

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

        String daysText = getRepeatDaysText(meal.getRepeatDays());
        holder.homeMealScheduleDate.setText(daysText);

        holder.scheduleActiveSwitch.setChecked(meal.isActive());

    }

    private String getRepeatDaysText(Map<String, Boolean> repeatDays) {
        if (repeatDays != null && !repeatDays.containsValue(false)) {
            return "All Days";
        } else {
            boolean weekdaysOnly = true;
            for (String day : repeatDays.keySet()) {
                if ((day.equals("Saturday") || day.equals("Sunday")) && repeatDays.get(day)) {
                    weekdaysOnly = false;
                    break;
                }
                if (!repeatDays.get(day) && (day.equals("Monday") || day.equals("Tuesday") || day.equals("Wednesday") || day.equals("Thursday") || day.equals("Friday"))) {
                    weekdaysOnly = false;
                    break;
                }
            }
            if (weekdaysOnly) {
                return "Monday to Friday";
            } else {
                StringBuilder daysBuilder = new StringBuilder();
                for (String day : repeatDays.keySet()) {
                    if (repeatDays.get(day)) {
                        daysBuilder.append(day.substring(0, 3)).append(", ");
                    }
                }
                if (daysBuilder.length() > 0) {
                    daysBuilder.setLength(daysBuilder.length() - 2);
                }
                return daysBuilder.toString();
            }
        }
    }

    // ViewHolder class for holding the views of each note card.
    public static class MealScheduleViewHolder extends RecyclerView.ViewHolder {
        public final TextView homeMealScheduleHour;
        public final TextView homeMealScheduleDate;
        public final Switch scheduleActiveSwitch;

        public MealScheduleViewHolder(@NonNull View homeMealView) {
            super(homeMealView);
            // Initialize the TextViews for the title and body of the note.
            this.homeMealScheduleHour = homeMealView.findViewById(R.id.homeMealScheduleHour);
            this.homeMealScheduleDate = homeMealView.findViewById(R.id.homeMealScheduleDate);
            this.scheduleActiveSwitch = homeMealView.findViewById(R.id.scheduleActiveSwitch);
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