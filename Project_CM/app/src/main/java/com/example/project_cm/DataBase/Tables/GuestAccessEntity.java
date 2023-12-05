package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "guest_accesses",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = HouseEntity.class,
                        parentColumns = "id",
                        childColumns = "house_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class GuestAccessEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "house_id")
    public int houseId;

    @ColumnInfo(name = "access_code")
    public String accessCode;

    @ColumnInfo(name = "access_status")
    public String accessStatus;

}
