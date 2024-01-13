package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


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

    public String getVaccineName() {
        return vaccineName;
    }

    public String getVaccineDate() {
        Date date = new Date(vaccineDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public int getDaysLeft() {
        Date vaccineDate = new Date(this.vaccineDate);
        Date currentDate = new Date();

        long diffInMillis = Math.abs(vaccineDate.getTime() - currentDate.getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        return (int) diffInDays;
    }
}