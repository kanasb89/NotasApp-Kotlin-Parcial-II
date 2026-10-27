package com.notasapp.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notasapp.data.model.NoteResponse
import com.notasapp.data.repository.NoteRepository
import com.notasapp.utils.Resource
import kotlinx.coroutines.launch

class CreateNoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _createState = MutableLiveData<Resource<NoteResponse>>()
    val createState: LiveData<Resource<NoteResponse>> = _createState

    fun createNote(title: String, content: String) {
        if (_createState.value is Resource.Loading) return

        _createState.value = Resource.Loading
        viewModelScope.launch {
            _createState.value = repository.createNote(title, content)
        }
    }
}
