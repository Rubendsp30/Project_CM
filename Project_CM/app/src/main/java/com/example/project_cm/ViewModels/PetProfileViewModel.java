package com.example.project_cm.ViewModels;

import androidx.lifecycle.ViewModel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.example.project_cm.DataBase.PetProfileDao;
import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class PetProfileViewModel extends ViewModel {
    private PetProfileDao petProfileDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public PetProfileViewModel(PetProfileDao petProfileDao) {
        this.petProfileDao = petProfileDao;
    }

    public void insertPetProfile(PetProfileEntity petProfile) {
        executor.execute(() -> petProfileDao.insertPetProfile(petProfile));
    }

    public void updatePetProfile(PetProfileEntity petProfile) {
        executor.execute(() -> petProfileDao.updatePetProfile(petProfile));
    }

    public void deletePetProfile(PetProfileEntity petProfile) {
        executor.execute(() -> petProfileDao.deletePetProfile(petProfile));
    }

    public LiveData<List<PetProfileEntity>> getPetProfilesByUserId(String userId) {
        MutableLiveData<List<PetProfileEntity>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<PetProfileEntity> profiles = petProfileDao.getPetProfilesByUserId(userId);
            liveData.postValue(profiles);
        });
        return liveData;
    }
}

