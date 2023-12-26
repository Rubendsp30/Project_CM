package com.example.project_cm;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class MealSchedule implements Serializable {
    private List<Meal> meals;
    private boolean[] repeatDays;
    private boolean isActive;
    private boolean notification;

    public MealSchedule(boolean[] repeatDays, boolean isActive, boolean notification) {
        this.meals = new ArrayList<>();
        this.repeatDays = repeatDays;
        this.isActive = isActive;
        this.notification = notification;
    }
    public void addMeal(Meal meal) {
        meals.add(meal);
    }

    // Getters and setters
    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public boolean[] getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(boolean[] repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

}

class Meal implements Serializable {
    private Calendar mealTime;
    private float portionSize;

    public Meal(Calendar mealTime, float portionSize) {
        this.mealTime = mealTime;
        this.portionSize = portionSize;
    }


    public Calendar getMealTime() {
        return mealTime;
    }

    public void setMealTime(Calendar mealTime) {
        this.mealTime = mealTime;
    }

    public float getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(float portionSize) {
        this.portionSize = portionSize;
    }
}
