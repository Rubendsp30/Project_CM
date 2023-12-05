package com.example.project_cm.ViewModels;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.project_cm.DataBase.AppDatabase;
import com.example.project_cm.DataBase.Tables.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserViewModel extends AndroidViewModel {

    private final MutableLiveData<List<UserEntity>> listOfUserEntity;
    private final AppDatabase appDatabase;
    private final ExecutorService executorService;

    public UserViewModel(Application application) {
        super(application);
        listOfUserEntity = new MutableLiveData<>();
        appDatabase = AppDatabase.getDBinstance(getApplication().getApplicationContext());

        executorService = Executors.newSingleThreadExecutor();
    }

    public MutableLiveData<List<UserEntity>> getListOfUserEntity() {
        return listOfUserEntity;
    }

    public void getAllUserEntityList(){
        List<UserEntity> userEntityList= appDatabase.userDao().getAllUsersList();
        if(userEntityList.size()>0){
            listOfUserEntity.postValue(userEntityList);
        }else{
            listOfUserEntity.postValue(null);
        }
    }

    public void insertUserEntity(UserEntity userEntity){
        executorService.execute(() -> {
            long rowId = appDatabase.userDao().insertUserEntity(userEntity);
            if (rowId > 0) {
                // Insertion was successful
                // You can use LiveData or another method to notify the UI
                Log.e("Insert User", "SUCESS");
            } else {
                // Insertion failed
                Log.e("Insert User", "FAIL");
            }
            getAllUserEntityList();
        });
    }

    public void checkUserExists(String username, Consumer<Integer> callback) {
        executorService.execute(() -> {
            int count = appDatabase.userDao().countUsersByUsername(username);
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(count));
        });
    }

    public void getUserByUsernameAndPassword(String username, String password, Consumer<UserEntity> callback) {
        executorService.execute(() -> {
            UserEntity user = appDatabase.userDao().getUserByUsernameAndPassword(username, password);
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(user));
        });
    }

    public void updateUserEntity(UserEntity userEntity){
        appDatabase.userDao().updateUserEntity(userEntity);
        getAllUserEntityList();
    }

    public void deleteUserEntity(UserEntity userEntity){
        appDatabase.userDao().deleteUserEntity(userEntity);
        getAllUserEntityList();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
