package com.example.project_cm.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FoodInfoViewModel extends ViewModel {

    private MutableLiveData<Double> temperature = new MutableLiveData<>();
    private MutableLiveData<Double> humidity = new MutableLiveData<>();
    private MutableLiveData<Integer> mealsLeft = new MutableLiveData<>();
    private MutableLiveData<Integer> daysLeft = new MutableLiveData<>();

    // Getters for LiveData objects
    public LiveData<Double> getTemperature() { return temperature; }
    public LiveData<Double> getHumidity() { return humidity; }
    public LiveData<Integer> getMealsLeft() { return mealsLeft; }
    public LiveData<Integer> getDaysLeft() { return daysLeft; }

    // Update LiveData objects
    public void setTemperature(double value) { temperature.setValue(value); }
    public void setHumidity(double value) { humidity.setValue(value); }
    public void setMealsLeft(int value) { mealsLeft.setValue(value); }
    public void setDaysLeft(int value) { daysLeft.setValue(value); }

}
