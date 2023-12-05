package com.example.project_cm.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project_cm.DataBase.Tables.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Query("Select * from users")
    List<UserEntity> getAllUsersList();

    @Insert
    long insertUserEntity (UserEntity user);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countUsersByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    UserEntity getUserByUsernameAndPassword(String username, String password);

    @Update
    void updateUserEntity (UserEntity userEntity);

    @Delete
    void deleteUserEntity (UserEntity userEntity);

}
