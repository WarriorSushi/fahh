package com.fahh.data.model

data class Sound(
    val name: String,
    val resId: Int,
    val icon: String,
    val isLocked: Boolean = false,
    val packName: String = "Free"
)
