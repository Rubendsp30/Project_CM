package com.example.project_cm.ViewModels;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.project_cm.Device;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

    public class DeviceManagerViewModel extends ViewModel {
        private static final String DEVICES_COLLECTION = "DEVICES";
        private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        private final MutableLiveData<List<Device>> devicesLiveData = new MutableLiveData<>();

        public void checkUserHasDevice(String userId) {
            firestore.collection(DEVICES_COLLECTION).whereEqualTo("user_id", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            List<Device> deviceList = new ArrayList<>();
                            task.getResult().forEach(document -> {
                                Device device = document.toObject(Device.class);
                                deviceList.add(device);
                            });
                            Log.d("DeviceManagerVM", "Dispositivos encontrados: " + deviceList.size());
                            devicesLiveData.postValue(deviceList);
                        }
                    });
        }

        public LiveData<List<Device>> getDevicesLiveData() {
            return devicesLiveData;
        }
    }

