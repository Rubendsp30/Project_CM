package com.example.project_cm.ViewModels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.DataBase.PetProfileDao;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.DataBase.Tables.VaccineEntity;
import com.example.project_cm.DataBase.VaccineDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VaccinesViewModel extends ViewModel {

    private VaccineDao vaccineDao;
    private final ExecutorService executorService;
    private MutableLiveData<List<VaccineEntity>> vaccines;

    public interface InsertCallback {
        void onInsertCompleted(long vaccineId);
    }

    public VaccinesViewModel(Application application) {
        vaccines = new MutableLiveData<>();

        AppDatabase database = AppDatabase.getDBinstance(application.getApplicationContext());
        vaccineDao = database.VaccineDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<VaccineEntity>> getVaccines() {

        return vaccines;
    }

    public LiveData<List<VaccineEntity>> getVaccinesByPetProfileId(int petProfileId) {
        return vaccineDao.getVaccinesForPet(petProfileId);
    }

    public void insertVaccine(VaccineEntity vaccine, InsertCallback callback) {
        executorService.execute(() -> {
            long rowId = vaccineDao.insertVaccineEntity(vaccine);
            callback.onInsertCompleted(rowId);
        });
    }

    public void setVaccines(List<VaccineEntity> vaccineList) {
        vaccines.setValue(vaccineList);
    }
}