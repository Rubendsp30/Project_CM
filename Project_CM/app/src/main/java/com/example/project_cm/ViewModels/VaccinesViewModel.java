package com.example.project_cm.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_cm.DataBase.Tables.VaccineEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VaccinesViewModel extends ViewModel {

    private MutableLiveData<List<VaccineEntity>> vaccines;

    public VaccinesViewModel() {
        vaccines = new MutableLiveData<>();
        // inicializar dados
    }

    public LiveData<List<VaccineEntity>> getVaccines() {
        return vaccines;
    }

    public void setVaccines(List<VaccineEntity> vaccineList) {
        vaccines.setValue(vaccineList);
    }
}