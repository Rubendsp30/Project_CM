package com.example.project_cm.ViewModels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.project_cm.MealSchedule;
import java.util.List;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.Device;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class FoodInfoViewModel extends AndroidViewModel {
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
        listenToDeviceUpdates();
    }

    //Listen to updates in Firestore
    private void listenToDeviceUpdates() {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            Log.e("FoodInfoVM", "Device ID is null or empty");
            return;
        }
        DocumentReference docRef = firestore.collection("DEVICES").document(deviceId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("FoodInfoVM", "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Device device = snapshot.toObject(Device.class);
                if (device != null) {
                    temperature.postValue(device.getSensor_temperature());
                    humidity.postValue(device.getSensor_humidity());
                    foodSupply.postValue(device.getFoodSuply());
                }
            } else {
                Log.d("FoodInfoVM", "Current data: null");
            }
        });
    }

    // Calculate remaining meals and days based on food supply and schedules
    public void calculateMealsAndDaysLeft(int totalFoodSupplyGrams, List<MealSchedule> mealSchedules) {

        //Keep track of remaining food supply, total meals and days
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
        Log.d("FoodInfoVM", "Days with meals: " + daysWithMeals + ", Total meals: " + totalMeals);

        // Update LiveData with the calculated values
        this.daysLeft.postValue(daysWithMeals);
        this.mealsLeft.postValue(totalMeals);
    }

    //I really hope it works, it seems to be working, need to do more tests

    // Getters
    public LiveData<Integer> getFoodSupply() { return foodSupply; }
    public LiveData<Double> getTemperature() { return temperature; }
    public LiveData<Double> getHumidity() { return humidity; }
    public LiveData<Integer> getmealsLeft() { return mealsLeft; }
    public LiveData<Integer> getdaysLeft() { return daysLeft; }
}
