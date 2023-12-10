package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


import java.util.Date;

@Entity(tableName = "vaccines",
        foreignKeys = @ForeignKey(entity = PetProfileEntity.class,
                parentColumns = "id",
                childColumns = "pet_id",
                onDelete = ForeignKey.CASCADE))
public class VaccineEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "pet_id")
    public int petId;

    @ColumnInfo(name = "vaccine_name")
    public String vaccineName;

    //ToDo Dps adicionar converters
    @ColumnInfo(name = "vaccine_date")
    public Long vaccineDate;

}
