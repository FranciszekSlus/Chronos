package com.tasker.chronos.data.repository

import android.content.Context
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class NotesData(
    val notes: List<Note> = emptyList(),
    val categories: List<NoteCategory> = emptyList()
)

class NotesRepository(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val file = File(context.filesDir, "notes.json")

    fun loadData(): NotesData {
        return try {
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<NotesData>(jsonString)
            } else {
                NotesData()
            }
        } catch (e: Exception) {
            android.util.Log.e("NotesRepository", "Error loading notes", e)
            NotesData()
        }
    }

    fun saveData(data: NotesData) {
        try {
            val jsonString = json.encodeToString(data)
            file.writeText(jsonString)
            android.util.Log.d("NotesRepository", "Saved ${data.notes.size} notes, ${data.categories.size} categories")
        } catch (e: Exception) {
            android.util.Log.e("NotesRepository", "Error saving notes", e)
        }
    }
}