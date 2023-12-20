package com.example.project_cm.ViewModels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cm.Callbacks.AuthCallback;
import com.example.project_cm.Callbacks.EmailCheckCallback;
import com.example.project_cm.Callbacks.UsernameCheckCallback;
import com.example.project_cm.Device;
import com.example.project_cm.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceViewModel extends ViewModel {

    private FirebaseFirestore firestore;
    private final MutableLiveData<Device> currentDevice = new MutableLiveData<>();
    private static final String DEVICES_COLLECTION = "DEVICES";
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public DeviceViewModel() {
        try {
            firestore = FirebaseFirestore.getInstance();
            // Disable Firestore cache
            /*FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build();
            firestore.setFirestoreSettings(settings);*/
        } catch (Exception e) {
            Log.e("DeviceViewModel", "Error initializing DeviceViewModel: " + e.getMessage());
        }
    }

    public void registerDevice(Device device){

        networkExecutor.execute(() -> {
            DocumentReference documentReference;
            documentReference = firestore.collection(DEVICES_COLLECTION).document();
            String newDeviceID = documentReference.getId();
            device.setDeviceID(newDeviceID);

            documentReference.set(device).addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        Log.e("registerDevice", "Failed to register device: " + e.getMessage());
                    });
        });
    }

    // Getter for currentDevice
    public MutableLiveData<Device> getCurrentDevice() {
        return currentDevice;
    }

    // Method to set the current device
    public void setCurrentDevice(Device device) {
        currentDevice.setValue(device);
    }


}
