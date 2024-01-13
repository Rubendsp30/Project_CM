package com.example.project_cm;


import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class MealSchedule implements Serializable {
    private String mealScheduleId;
    private Date mealTime;
    private Map<String, Boolean> repeatDays;
    private boolean isActive;
    private boolean notification;
    private int portionSize;


    public MealSchedule(Date mealTime, Map<String, Boolean> repeatDays, boolean isActive, boolean notification, int portionSize) {
        this.mealTime = mealTime;
        this.repeatDays = repeatDays;
        this.isActive = isActive;
        this.notification = notification;
        this.portionSize = portionSize;
    }

    public MealSchedule() {

    }

    public String getMealScheduleId() {
        return mealScheduleId;
    }

    public void setMealScheduleId(String mealScheduleId) {
        this.mealScheduleId = mealScheduleId;
    }

    public Date getMealTime() {
        return mealTime;
    }

    public void setMealTime(Date mealTime) {
        this.mealTime = mealTime;
    }

    public Map<String, Boolean> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(Map<String, Boolean> repeatDays) {
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

    public int getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(int portionSize) {
        this.portionSize = portionSize;
    }
}
