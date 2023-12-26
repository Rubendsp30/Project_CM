package com.example.project_cm.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.project_cm.DataBase.Tables.PetProfileEntity;
import com.example.project_cm.DataBase.Tables.VaccineEntity;

@Database(entities = {VaccineEntity.class, PetProfileEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    //public abstract UserDao userDao();
    public abstract PetProfileDao petProfileDao();
    public abstract VaccineDao VaccineDao();
    public static AppDatabase INSTANCE;

    public static AppDatabase getDBinstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "AppDatabase").build();

        }
        return INSTANCE;
    }
}
