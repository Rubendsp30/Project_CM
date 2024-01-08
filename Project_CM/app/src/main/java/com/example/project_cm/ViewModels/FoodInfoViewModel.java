package com.example.project_cm.ViewModels;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.project_cm.ViewModels.DeviceViewModel;
import com.example.project_cm.Device;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;


public class FoodInfoViewModel extends AndroidViewModel {

    private MutableLiveData<Double> temperature = new MutableLiveData<>();
    private MutableLiveData<Double> humidity = new MutableLiveData<>();
    private MutableLiveData<Integer> foodSupply = new MutableLiveData<>();

    private FirebaseFirestore firestore;
    private String deviceId;

    public FoodInfoViewModel(@NonNull Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        listenToDeviceUpdates();
    }

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

    // Getters for LiveData objects
    public LiveData<Integer> getFoodSupply() { return foodSupply; }
    public LiveData<Double> getTemperature() { return temperature; }
    public LiveData<Double> getHumidity() { return humidity; }
}
