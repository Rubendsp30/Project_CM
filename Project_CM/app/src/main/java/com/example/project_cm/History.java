package com.example.project_cm;

import java.io.Serializable;
import java.util.Date;

public class History implements Serializable {
    private Date meal_time;
    private int quantity_served;

    public History() {
    }

    public History(Date meal_time, int quantity_served) {
        this.meal_time = meal_time;
        this.quantity_served = quantity_served;
    }

    public Date getMealTime() {
        return meal_time;
    }

    public void setMealTime(Date meal_time) {
        this.meal_time = meal_time;
    }

    public int getQuantityServed() {
        return quantity_served;
    }

    public void setQuantityServed(int quantity_served) {
        this.quantity_served = quantity_served;
    }
}

