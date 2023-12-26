package com.example.project_cm.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project_cm.DataBase.Tables.VaccineEntity;

import java.util.List;

@Dao
public interface VaccineDao {

    @Query("Select * from vaccines")
    List<VaccineEntity> getAllVaccinesList();

    @Insert
    long insertUserEntity (VaccineEntity vaccine);

    /*
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countUsersByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    UserEntity getUserByUsernameAndPassword(String username, String password);
    */

    @Update
    void updateUserEntity (VaccineEntity vaccineEntity);

    @Delete
    void deleteUserEntity (VaccineEntity vaccineEntity);

}
