package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "devices",
        foreignKeys = {
                @ForeignKey(entity = HouseEntity.class,
                        parentColumns = "id",
                        childColumns = "house_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = PetProfileEntity.class,
                        parentColumns = "id",
                        childColumns = "pet_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class DeviceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "house_id")
    public int houseId;

    @ColumnInfo(name = "pet_id")
    public int petId;

    /*@ColumnInfo(name = "status")
    public int status;*/

    @ColumnInfo(name = "temperature_data")
    public float temperatureData;

    @ColumnInfo(name = "humidity_data")
    public float humidityData;

}
