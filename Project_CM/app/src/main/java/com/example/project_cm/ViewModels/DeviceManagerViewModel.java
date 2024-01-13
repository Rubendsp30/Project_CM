package com.example.project_cm.ViewModels;
import androidx.lifecycle.AndroidViewModel;
import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.DataBase.PetProfileDao;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.util.Log;

    public class DeviceManagerViewModel extends AndroidViewModel {
        private static final String DEVICES_COLLECTION = "DEVICES";
        private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        private final PetProfileDao petProfileDao;
        private final ExecutorService executorService;

        public DeviceManagerViewModel(Application application) {
            super(application);
            AppDatabase database = AppDatabase.getDBinstance(application.getApplicationContext());
            petProfileDao = database.petProfileDao();
            executorService = Executors.newSingleThreadExecutor();
        }

        public void deleteDevice(String deviceId, int petId) {
            firestore.collection(DEVICES_COLLECTION).document(deviceId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DeviceManagerVM", "Device successfully deleted from Firestore");
                        deleteAssociatedPetAndVaccines(petId);
                    })
                    .addOnFailureListener(e -> Log.w("DeviceManagerVM", "Error deleting device", e));
        }

        private void deleteAssociatedPetAndVaccines(int petId) {

            executorService.execute(() -> {
                PetProfileEntity pet = petProfileDao.getPetProfileById(petId);
                if (pet != null) {
                    petProfileDao.deletePetProfile(pet);

                    Log.d("DeviceManagerVM", "Associated pet and vaccines deleted from Room");
                }
            });
        }


    }

