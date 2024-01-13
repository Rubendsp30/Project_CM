package com.example.project_cm.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project_cm.DataBase.Tables.VaccineEntity;

import java.util.List;

@Dao
public interface VaccineDao {

    @Insert
    long insertVaccineEntity(VaccineEntity vaccine);

    @Query("SELECT * FROM vaccines WHERE pet_id = :petId")
    LiveData<List<VaccineEntity>> getVaccinesForPet(int petId);

    @Update
    void updateVaccineEntity(VaccineEntity vaccineEntity);

    @Delete
    void deleteVaccineEntity(VaccineEntity vaccineEntity);

}