package com.example.project_cm.ViewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.DataBase.VaccineDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaccinesViewModel extends AndroidViewModel {

    private final VaccineDao vaccineDao;
    private final ExecutorService executorService;

    public VaccinesViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = AppDatabase.getDBinstance(application.getApplicationContext());
        vaccineDao = database.vaccineDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<VaccineEntity>> getVaccinesByPetProfileId(int petProfileId) {
        return vaccineDao.getVaccinesForPet(petProfileId);
    }

   public void insertVaccine(VaccineEntity vaccine) {
       executorService.execute(() -> {
           vaccineDao.insertVaccineEntity(vaccine);
       });
   }
    public void updateVaccine(VaccineEntity vaccine) {
        executorService.execute(() -> {
            vaccineDao.updateVaccineEntity(vaccine);
        });
    }

    public void deleteVaccine(VaccineEntity vaccine) {
        executorService.execute(() -> vaccineDao.deleteVaccineEntity(vaccine));
    }

}