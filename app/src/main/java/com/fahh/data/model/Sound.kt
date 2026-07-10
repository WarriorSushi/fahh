package com.fahh.data.model

data class Sound(
    val name: String,
    val resId: Int,
    val icon: String,
    val isLocked: Boolean = false,
    val packName: String = "Free",
    /** Stable catalog key. Never derive entitlement or persistence from the display name. */
    val id: String = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_'),
    val filePath: String? = null
)
