package com.example.project_cm.ViewModels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import com.example.project_cm.MealSchedule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleViewModel extends ViewModel {
    private FirebaseFirestore firestore;
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private MutableLiveData<Boolean> mealScheduleAdded = new MutableLiveData<>();

    public LiveData<Boolean> isMealScheduleAdded() {
        return mealScheduleAdded;
    }

    public ScheduleViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void addMealSchedule(String deviceId, MealSchedule schedule, MealScheduleCallback callback) {
        //TODO Adicionar o executor para correr em thread

        networkExecutor.execute(() -> {
            DocumentReference scheduleRef = firestore.collection("DEVICES").document(deviceId).collection("MEAL_SCHEDULES").document();
            schedule.setMealScheduleId(scheduleRef.getId());
            scheduleRef.set(schedule)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("addMealSchedule", "Meal schedule added successfully");
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("addMealSchedule", "Error adding meal schedule", e);
                        if (callback != null) callback.onFailure();
                    });
        });
    }

    public interface MealScheduleCallback {
        void onSuccess();
        void onFailure();
    }


    public LiveData<List<MealSchedule>> getMealSchedulesForDevice(String deviceId) {
        MutableLiveData<List<MealSchedule>> mealSchedulesLiveData = new MutableLiveData<>();

        networkExecutor.execute(() -> {
            firestore.collection("DEVICES").document(deviceId).collection("MEAL_SCHEDULES")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ArrayList<MealSchedule> mealSchedules = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            MealSchedule schedule = snapshot.toObject(MealSchedule.class);
                            mealSchedules.add(schedule);
                        }
                        uiHandler.post(() -> mealSchedulesLiveData.setValue(mealSchedules));
                    })
                    .addOnFailureListener(e -> Log.e("getMealSchedulesForDevice", "Error getting meal schedules: " + e.getMessage()));
        });

        return mealSchedulesLiveData;
    }

}