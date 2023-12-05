package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "meal_schedules",
        foreignKeys = @ForeignKey(entity = DeviceEntity.class,
                parentColumns = "id",
                childColumns = "device_id",
                onDelete = ForeignKey.CASCADE))
public class MealScheduleEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "device_id")
    public int deviceId;

    @ColumnInfo(name = "meal_time")
    public Date mealTime;

    @ColumnInfo(name = "repeat_days")
    public String repeatDays;

    @ColumnInfo(name = "portion_size")
    public float portionSize;

}