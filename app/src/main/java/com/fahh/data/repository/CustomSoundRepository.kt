package com.fahh.data.repository

import android.content.Context
import com.fahh.data.model.Sound
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomSoundRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val preferences = context.getSharedPreferences("custom_sounds", Context.MODE_PRIVATE)
    private val _sounds = MutableStateFlow(load())
    val sounds: StateFlow<List<Sound>> = _sounds

    fun save(name: String, file: File): Sound {
        val sound = Sound(
            name = name.trim().ifBlank { "My Fahh" },
            resId = 0,
            icon = "✦",
            packName = "Custom sounds",
            id = "custom_${UUID.randomUUID()}",
            filePath = file.absolutePath
        )
        _sounds.value = _sounds.value + sound
        persist()
        return sound
    }

    fun delete(soundId: String) {
        val removed = _sounds.value.firstOrNull { it.id == soundId } ?: return
        removed.filePath?.let { File(it).delete() }
        _sounds.value = _sounds.value.filterNot { it.id == soundId }
        persist()
    }

    private fun load(): List<Sound> = runCatching {
        val entries = JSONArray(preferences.getString("items", "[]"))
        buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.getJSONObject(index)
                val path = entry.getString("path")
                if (File(path).exists()) {
                    add(Sound(entry.getString("name"), 0, "✦", false, "Custom sounds", entry.getString("id"), path))
                }
            }
        }
    }.getOrDefault(emptyList())

    private fun persist() {
        val entries = JSONArray()
        _sounds.value.forEach { sound ->
            entries.put(JSONObject().apply {
                put("id", sound.id)
                put("name", sound.name)
                put("path", sound.filePath)
            })
        }
        preferences.edit().putString("items", entries.toString()).apply()
    }
}
