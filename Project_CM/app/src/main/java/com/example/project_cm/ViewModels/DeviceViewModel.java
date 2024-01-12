package com.example.project_cm.ViewModels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cm.Callbacks.AuthCallback;
import com.example.project_cm.Callbacks.EmailCheckCallback;
import com.example.project_cm.Callbacks.UsernameCheckCallback;
import com.example.project_cm.Device;
import com.example.project_cm.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceViewModel extends ViewModel {

    private FirebaseFirestore firestore;
    //private final MutableLiveData<Device> currentDevice = new MutableLiveData<>();
    private  String currentDeviceId;
    private static final String DEVICES_COLLECTION = "DEVICES";
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<String> newDeviceId = new MutableLiveData<>();




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
            documentReference = firestore.collection(DEVICES_COLLECTION).document(newDeviceId.getValue());
            device.setDeviceID(newDeviceId.getValue());

            documentReference.set(device).addOnSuccessListener(aVoid -> {

                    })
                    .addOnFailureListener(e -> {
                        Log.e("registerDevice", "Failed to register device: " + e.getMessage());
                    });
        });
    }

    public LiveData<ArrayList<Device>> getDevicesForUser(String userId) {
        MutableLiveData<ArrayList<Device>> devicesLiveData = new MutableLiveData<>();

        networkExecutor.execute(() -> {
            firestore.collection(DEVICES_COLLECTION)
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ArrayList<Device> devices = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            Device device = snapshot.toObject(Device.class);
                            devices.add(device);
                        }
                        uiHandler.post(() -> devicesLiveData.setValue(devices));
                    })
                    .addOnFailureListener(e -> Log.e("getDevicesForUser", "Error getting devices for user: " + e.getMessage()));
        });

        return devicesLiveData;
    }

    public interface DeviceIdLoadCallback {
        void onDeviceIdLoaded(String DeviceId);
        void onError(Exception e);
    }
    
    public void getDevicesForUserWidget(String userId, DeviceIdLoadCallback callback) {
        firestore.collection(DEVICES_COLLECTION)
                .whereEqualTo("user_id", userId)
                .orderBy("pet_id", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document's device ID
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Device device = snapshot.toObject(Device.class);
                        if (device != null) {
                            callback.onDeviceIdLoaded(device.getDeviceID());
                        } else {
                            callback.onError(new Exception("Device data is null"));
                        }
                    } else {
                        callback.onError(new Exception("No device found for user"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }



    public LiveData<ArrayList<Device>> listenForDeviceUpdates(String userId) {
        MutableLiveData<ArrayList<Device>> devicesLiveData = new MutableLiveData<>();

        firestore.collection(DEVICES_COLLECTION)
                .whereEqualTo("user_id", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("listenForDeviceUpdates", "Listen failed", e);
                        return;
                    }
                    ArrayList<Device> devices = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        devices.add(doc.toObject(Device.class));
                    }
                    devicesLiveData.postValue(devices);
                });

        return devicesLiveData;
    }

    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    public void setCurrentDeviceId(String currentDeviceId) {
        this.currentDeviceId = currentDeviceId;
    }

    public void setNewDeviceId(String deviceId) {
        newDeviceId.setValue(deviceId);
    }

    public LiveData<String> getNewDeviceId() {
        return newDeviceId;
    }


}
