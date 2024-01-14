package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        Calendar calendarVaccine = Calendar.getInstance();
        calendarVaccine.setTime(new Date(this.vaccineDate));
        calendarVaccine.set(Calendar.HOUR_OF_DAY, 0);
        calendarVaccine.set(Calendar.MINUTE, 0);
        calendarVaccine.set(Calendar.SECOND, 0);
        calendarVaccine.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffInMillis = Math.abs(calendarVaccine.getTimeInMillis() - today.getTimeInMillis());
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        return (int) diffInDays;
    }
}