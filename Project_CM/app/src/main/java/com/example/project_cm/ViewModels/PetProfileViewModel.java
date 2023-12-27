        package com.example.project_cm.ViewModels;

        import android.app.Application;

        import androidx.lifecycle.AndroidViewModel;
        import androidx.lifecycle.LiveData;
        import androidx.lifecycle.MutableLiveData;

        import com.example.project_cm.DataBase.AppDatabase;
        import com.example.project_cm.DataBase.Tables.PetProfileEntity;
        import com.example.project_cm.DataBase.PetProfileDao;

        import java.util.List;
        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;

        public class PetProfileViewModel extends AndroidViewModel {
            private PetProfileDao petProfileDao;
            private final ExecutorService executorService;

            public PetProfileViewModel(Application application) {
                super(application);
                AppDatabase database = AppDatabase.getDBinstance(application.getApplicationContext());
                petProfileDao = database.petProfileDao();
                executorService = Executors.newSingleThreadExecutor();
            }
        public void insertPetProfile(PetProfileEntity petProfile) {
            executorService.execute(() -> petProfileDao.insertPetProfile(petProfile));
        }

        public void updatePetProfile(PetProfileEntity petProfile) {
            executorService.execute(() -> petProfileDao.updatePetProfile(petProfile));
        }

        public void deletePetProfile(PetProfileEntity petProfile) {
            executorService.execute(() -> petProfileDao.deletePetProfile(petProfile));
        }


            public LiveData<List<PetProfileEntity>> getPetProfilesByUserId(String userId) {
                MutableLiveData<List<PetProfileEntity>> liveData = new MutableLiveData<>();
                executorService.execute(() -> {
                    List<PetProfileEntity> profiles = petProfileDao.getPetProfilesByUserId(userId);
                    liveData.postValue(profiles);
                });
                return liveData;
            }
            public LiveData<PetProfileEntity> getPetProfileById(String petProfileId) {
                MutableLiveData<PetProfileEntity> liveData = new MutableLiveData<>();
                executorService.execute(() -> {
                    PetProfileEntity profile = petProfileDao.getPetProfileById(petProfileId);
                    liveData.postValue(profile);
                });
                return liveData;
            }


            @Override
        protected void onCleared() {
            super.onCleared();
            executorService.shutdown();
        }
    }

