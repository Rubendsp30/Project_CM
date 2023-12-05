package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

import java.util.Date;

@Entity(tableName = "meal_histories",
        foreignKeys = @ForeignKey(entity = PetProfileEntity.class,
                parentColumns = "id",
                childColumns = "pet_id",
                onDelete = ForeignKey.CASCADE))
public class MealHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "pet_id")
    public int petId;

    @ColumnInfo(name = "meal_time")
    public Date mealTime;

    @ColumnInfo(name = "quantity_served")
    public float quantityServed;

    @ColumnInfo(name = "feed_type")
    public String feedType;

}
