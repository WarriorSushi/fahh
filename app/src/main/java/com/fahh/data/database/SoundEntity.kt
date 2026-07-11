package com.fahh.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey val soundId: String,
    val name: String,
    val resId: Int,
    val icon: String,
    val isLocked: Boolean,
    val packName: String
)
