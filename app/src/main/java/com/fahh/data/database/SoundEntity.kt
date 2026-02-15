package com.fahh.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val resId: Int,
    val icon: String,
    val isLocked: Boolean,
    val packName: String
)
