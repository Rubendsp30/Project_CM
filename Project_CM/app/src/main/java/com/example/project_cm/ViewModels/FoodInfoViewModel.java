package com.example.project_cm.ViewModels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.project_cm.MealSchedule;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;
import com.example.project_cm.Device;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import android.os.Handler;
import android.os.Looper;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class FoodInfoViewModel extends AndroidViewModel {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    //LiveData
    private MutableLiveData<Double> temperature = new MutableLiveData<>();
    private MutableLiveData<Double> humidity = new MutableLiveData<>();
    private MutableLiveData<Integer> mealsLeft = new MutableLiveData<>();
    private MutableLiveData<Integer> daysLeft = new MutableLiveData<>();
    private MutableLiveData<Integer> foodSupply = new MutableLiveData<>();

    private FirebaseFirestore firestore;
    private String deviceId;

    public FoodInfoViewModel(@NonNull Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
    }
    //Set deviceId and listen to updates
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        listenToDeviceUpdates(deviceId);
    }

    //Listen to updates in Firestore
    public void listenToDeviceUpdates(String deviceId) {
        DocumentReference docRef = firestore.collection("DEVICES").document(deviceId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                executorService.execute(() -> {
                    Device device = snapshot.toObject(Device.class);
                    if (device != null) {
                        mainThreadHandler.post(() -> {
                            temperature.postValue(device.getSensor_temperature());
                            humidity.postValue(device.getSensor_humidity());
                            foodSupply.postValue(device.getFoodSuply());
                        });
                    }
                });
            }
        });
    }

    // Calculate remaining meals and days based on food supply and schedules
    public void calculateMealsAndDaysLeft(List<MealSchedule> mealSchedules) {
        executorService.execute(() -> {

            //Keep track of remaining food supply, total meals and days
            int totalFoodSupplyGrams = (foodSupply.getValue());
            int remainingFoodSupply = totalFoodSupplyGrams;
            int totalMeals = 0;

            Set<String> uniqueDays = new HashSet<>();
            String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            //Each day of the week
            for (String day : daysOfWeek) {
                //Each meal schedule
                for (MealSchedule schedule : mealSchedules) {
                    // Iterate through the days of the week specified in the meal schedule
                    for (Map.Entry<String, Boolean> entry : schedule.getRepeatDays().entrySet()) {
                        // Check if the meal is scheduled for the current day and there's enough food supply
                        if (entry.getValue() && remainingFoodSupply >= schedule.getPortionSize()) {
                            // Deduct the portion size from the remaining food supply
                            remainingFoodSupply -= schedule.getPortionSize();
                            // Increment the total meals count
                            totalMeals++;
                            // Add the current day to the set of unique days
                            uniqueDays.add(entry.getKey());
                        }
                    }
                }
            }
            // Calculate the number of days with meals
            int daysWithMeals = uniqueDays.size();

            final int finalTotalMeals = totalMeals;
            final int finalDaysWithMeals = daysWithMeals;

            // Update LiveData with the calculated values
            mainThreadHandler.post(() -> {

                this.daysLeft.setValue(finalDaysWithMeals);
                this.mealsLeft.setValue(finalTotalMeals);
            });
        });
    }

    //I really hope it works, it seems to be working with the logs, but I need to test with real values
    //of the devices

    // Getters
    public LiveData<Integer> getFoodSupply() {
        return foodSupply;
    }

    public LiveData<Double> getTemperature() {
        return temperature;
    }

    public LiveData<Double> getHumidity() {
        return humidity;
    }

    public LiveData<Integer> getmealsLeft() {
        return mealsLeft;
    }

    public LiveData<Integer> getdaysLeft() {
        return daysLeft;
    }

}