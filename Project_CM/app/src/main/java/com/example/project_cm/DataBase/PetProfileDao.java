package com.example.project_cm.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;

import java.util.List;

@Dao
public interface PetProfileDao {


    @Insert
    long insertPetProfile(PetProfileEntity petProfile);

    @Query("SELECT * FROM pet_profiles WHERE user_id = :userId")
    List<PetProfileEntity> getPetProfilesByUserId(String userId);

    @Query("SELECT * FROM pet_profiles WHERE id = :petProfileId")
    PetProfileEntity getPetProfileById(long petProfileId);


    @Update
    void updatePetProfile(PetProfileEntity petProfile);


    @Delete
    void deletePetProfile(PetProfileEntity petProfile);


}
