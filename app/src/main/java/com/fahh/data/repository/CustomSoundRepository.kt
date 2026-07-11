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
            name = name.trim().ifBlank { nextDefaultName() },
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

    fun update(soundId: String, name: String, replacementFile: File? = null): Sound {
        val existing = _sounds.value.firstOrNull { it.id == soundId }
            ?: error("Custom sound no longer exists.")
        val updated = existing.copy(
            name = name.trim().ifBlank { existing.name },
            filePath = replacementFile?.absolutePath ?: existing.filePath
        )
        _sounds.value = _sounds.value.map { if (it.id == soundId) updated else it }
        if (replacementFile != null && existing.filePath != replacementFile.absolutePath) {
            existing.filePath?.let { File(it).delete() }
        }
        persist()
        return updated
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

    private fun nextDefaultName(): String {
        val highestExistingNumber = _sounds.value.mapNotNull { sound ->
            Regex("^My sound (\\d+)$", RegexOption.IGNORE_CASE)
                .matchEntire(sound.name)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
        }.maxOrNull() ?: 0
        return "My sound ${highestExistingNumber + 1}"
    }

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
