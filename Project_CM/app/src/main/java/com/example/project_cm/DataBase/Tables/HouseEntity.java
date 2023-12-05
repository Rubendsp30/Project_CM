package com.example.project_cm.DataBase.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


@Entity(tableName = "houses",
        foreignKeys = @ForeignKey(entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "owner_user_id",
                onDelete = ForeignKey.CASCADE))
public class HouseEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "owner_user_id")
    public int ownerUserId;
}
