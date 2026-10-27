package com.notasapp.data.repository

import com.notasapp.data.api.ApiService
import com.notasapp.data.model.NoteRequest
import com.notasapp.data.model.NoteResponse
import com.notasapp.utils.Resource
import com.notasapp.utils.safeApiCall

class NoteRepository(private val apiService: ApiService) {

    suspend fun createNote(title: String, content: String): Resource<NoteResponse> {
        return safeApiCall { apiService.createNote(NoteRequest(title, content)) }
    }

    suspend fun getNotes(): Resource<List<NoteResponse>> {
        return safeApiCall { apiService.getNotes() }
    }

    suspend fun deleteNote(id: Int): Resource<Unit> {
        return safeApiCall { apiService.deleteNote(id) }
    }
}
