package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pet_profiles")
public class PetProfileEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    //ToDo: Ver como guardar imagem. Podemos guardar uma cópia da imagem e um path para ela. Ou diretamente path para a foto mas pode-se perder.idk
    /*@ColumnInfo(name = "photo")
    public String photo;*/

    //0 dog, 1 cat
    @ColumnInfo(name = "animal_type")
    public int animalType;

    @ColumnInfo(name = "breed")
    public String breed;

    @ColumnInfo(name = "age")
    public int age;

    @ColumnInfo(name = "weight")
    public float weight;

    //0 male, 1 female
    @ColumnInfo(name = "gender")
    public int gender;

    @ColumnInfo(name = "microchip_number")
    public String microchipNumber;

    @ColumnInfo(name = "user_id")
    public String userID;

}