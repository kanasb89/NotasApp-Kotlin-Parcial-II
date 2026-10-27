package com.notasapp.data.api

import com.notasapp.data.model.LoginRequest
import com.notasapp.data.model.LoginResponse
import com.notasapp.data.model.NoteRequest
import com.notasapp.data.model.NoteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("notes")
    suspend fun createNote(@Body request: NoteRequest): Response<NoteResponse>

    @GET("notes")
    suspend fun getNotes(): Response<List<NoteResponse>>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): Response<Unit>
}
