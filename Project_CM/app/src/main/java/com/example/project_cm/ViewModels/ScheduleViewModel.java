package com.example.project_cm.ViewModels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import com.example.project_cm.MealSchedule;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.firebase.firestore.EventListener;


public class ScheduleViewModel extends ViewModel {
    private FirebaseFirestore firestore;
    private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public ScheduleViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void addMealSchedule(String deviceId, MealSchedule schedule, MealScheduleCallback callback) {

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

        firestore.collection("DEVICES").document(deviceId).collection("MEAL_SCHEDULES")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("getMealSchedules", "Listen failed.", e);
                            return;
                        }

                        ArrayList<MealSchedule> mealSchedules = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                MealSchedule schedule = doc.toObject(MealSchedule.class);
                                mealSchedules.add(schedule);
                            }
                        }
                        mealSchedulesLiveData.postValue(mealSchedules);
                    }
                });

        return mealSchedulesLiveData;
    }


    public void deleteMealSchedule(String deviceId, String mealScheduleId) {

        networkExecutor.execute(() -> {
            DocumentReference scheduleRef = firestore.collection("DEVICES")
                    .document(deviceId)
                    .collection("MEAL_SCHEDULES")
                    .document(mealScheduleId);
            scheduleRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("deleteMealSchedule", "Meal schedule successfully deleted"))
                    .addOnFailureListener(e -> Log.e("deleteMealSchedule", "Error deleting meal schedule: " + e.getMessage()));
        });
    }

    public void updateMealScheduleActiveStatus(String deviceId, String mealScheduleId, boolean isActive, MealScheduleCallback callback) {
        networkExecutor.execute(() -> {
            DocumentReference scheduleRef = firestore.collection("DEVICES")
                    .document(deviceId)
                    .collection("MEAL_SCHEDULES")
                    .document(mealScheduleId);

            scheduleRef.update("active", isActive)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("updateActiveStatus", "Meal schedule active status updated successfully");
                        uiHandler.post(() -> {
                            if (callback != null) callback.onSuccess();
                            //getMealSchedulesForDevice(deviceId);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("updateActiveStatus", "Error updating meal schedule active status", e);
                        uiHandler.post(() -> {
                            if (callback != null) callback.onFailure();
                        });
                    });
        });
    }


}