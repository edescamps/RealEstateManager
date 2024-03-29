package com.openclassrooms.realestatemanager.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi_table")
data class Poi(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "poi_id", index = true)
    val poiId: Int,

    @ColumnInfo(name = "poi_name")
    var poiName: String,
)

