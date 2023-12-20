package com.example.project_cm;

import java.io.Serializable;

public class Device implements Serializable {
    private String user_id,pet_id,deviceID;
    private int status;
    private double sensor_temperature, sensor_humidity;

    public Device(String user_id) {
        this.user_id = user_id;
    }

    public Device(String user_id, String deviceID) {
        this.user_id = user_id;
        this.deviceID = deviceID;
    }

    public Device(String user_id, String pet_id, int status, double sensor_temperature, double sensor_humidity) {
        this.user_id = user_id;
        this.pet_id = pet_id;
        this.status = status;
        this.sensor_temperature = sensor_temperature;
        this.sensor_humidity = sensor_humidity;
    }

    public Device() {

    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPet_id() {
        return pet_id;
    }

    public void setPet_id(String pet_id) {
        this.pet_id = pet_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getSensor_temperature() {
        return sensor_temperature;
    }

    public void setSensor_temperature(double sensor_temperature) {
        this.sensor_temperature = sensor_temperature;
    }

    public double getSensor_humidity() {
        return sensor_humidity;
    }

    public void setSensor_humidity(double sensor_humidity) {
        this.sensor_humidity = sensor_humidity;
    }
}
