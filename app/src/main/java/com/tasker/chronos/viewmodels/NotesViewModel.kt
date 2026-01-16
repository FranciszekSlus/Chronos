package com.tasker.chronos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.tasker.chronos.data.models.Note
import com.tasker.chronos.data.models.NoteCategory
import com.tasker.chronos.data.repository.NotesRepository
import com.tasker.chronos.data.repository.NotesData

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotesRepository(application)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _categories = MutableStateFlow<List<NoteCategory>>(emptyList())
    val categories: StateFlow<List<NoteCategory>> = _categories.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val data = repository.loadData()
            _notes.value = data.notes
            _categories.value = data.categories
            android.util.Log.d("NotesViewModel", "Loaded ${data.notes.size} notes")
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            repository.saveData(NotesData(
                notes = _notes.value,
                categories = _categories.value
            ))
        }
    }

    fun addNote(note: Note) {
        _notes.value = _notes.value + note
        saveData()
        android.util.Log.d("NotesViewModel", "Added note: ${note.title}")
    }

    fun updateNote(note: Note) {
        _notes.value = _notes.value.map {
            if (it.id == note.id) note.copy(updatedAt = java.time.LocalDateTime.now())
            else it
        }
        saveData()
        android.util.Log.d("NotesViewModel", "Updated note: ${note.title}")
    }

    fun deleteNote(noteId: String) {
        _notes.value = _notes.value.filter { it.id != noteId }
        saveData()
        android.util.Log.d("NotesViewModel", "Deleted note: $noteId")
    }

    fun togglePin(noteId: String) {
        _notes.value = _notes.value.map {
            if (it.id == noteId) it.copy(isPinned = !it.isPinned)
            else it
        }
        saveData()
    }

    fun addCategory(category: NoteCategory) {
        _categories.value = _categories.value + category
        saveData()
        android.util.Log.d("NotesViewModel", "Added category: ${category.name}")
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        _notes.value = _notes.value.map {
            if (it.categoryId == categoryId) it.copy(categoryId = null)
            else it
        }
        saveData()
    }
}