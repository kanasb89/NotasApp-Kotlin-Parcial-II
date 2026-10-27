package com.notasapp.data.repository

import com.notasapp.data.api.ApiService
import com.notasapp.data.model.LoginRequest
import com.notasapp.data.model.LoginResponse
import com.notasapp.utils.Resource
import com.notasapp.utils.SessionManager
import com.notasapp.utils.safeApiCall

class LoginRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        val result = safeApiCall { apiService.login(LoginRequest(email, password)) }
        if (result is Resource.Success) {
            sessionManager.saveSession(result.data.token, result.data.user)
        }
        return result
    }
}
