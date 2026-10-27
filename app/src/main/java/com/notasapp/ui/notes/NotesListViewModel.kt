package com.notasapp.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.model.NoteResponse
import com.notasapp.data.repository.NoteRepository
import com.notasapp.utils.Resource
import kotlinx.coroutines.launch

class NotesListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notesState = MutableLiveData<Resource<List<NoteResponse>>>()
    val notesState: LiveData<Resource<List<NoteResponse>>> = _notesState

    private val _deleteState = MutableLiveData<Resource<Unit>?>()
    val deleteState: LiveData<Resource<Unit>?> = _deleteState

    fun loadNotes() {
        if (_notesState.value is Resource.Loading) return

        _notesState.value = Resource.Loading
        viewModelScope.launch {
            _notesState.value = repository.getNotes()
        }
    }

    fun deleteNote(id: Int) {
        if (_deleteState.value is Resource.Loading) return

        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            _deleteState.value = repository.deleteNote(id)
        }
    }

    fun consumeDeleteState() {
        _deleteState.value = null
    }
}
