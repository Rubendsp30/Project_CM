package com.example.project_cm.ViewModels;

import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.Calendar;
import java.util.HashMap;
import com.example.project_cm.MealSchedule;
import java.util.Map;

public class ScheduleViewModel extends ViewModel {
    private FirebaseFirestore firestore;

    public ScheduleViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void addMealSchedule(String deviceId, MealSchedule schedule) {
        DocumentReference scheduleRef = firestore.collection("DEVICES").document(deviceId).collection("MEAL_SCHEDULES").document();
        schedule.setMealScheduleId(scheduleRef.getId());
        scheduleRef.set(schedule);
    }
}