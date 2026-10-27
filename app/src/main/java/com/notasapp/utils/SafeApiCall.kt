package com.notasapp.utils

import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("El servidor devolvió una respuesta vacía")
            }
        } else {
            Resource.Error(extractErrorMessage(response))
        }
    } catch (e: IOException) {
        Resource.Error("No se pudo conectar con el servidor. Verifica tu conexión a internet")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Ocurrió un error inesperado")
    }
}

private fun <T> extractErrorMessage(response: Response<T>): String {
    val rawBody = try {
        response.errorBody()?.string()
    } catch (e: IOException) {
        null
    }

    val serverMessage = rawBody?.let {
        try {
            JSONObject(it).optString("message").takeIf { msg -> msg.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    return serverMessage ?: when (response.code()) {
        401 -> "Credenciales o sesión no válidas"
        403 -> "No tienes permiso para realizar esta acción"
        404 -> "Recurso no encontrado"
        in 500..599 -> "Error interno del servidor. Intenta más tarde"
        else -> "Error de comunicación (código ${response.code()})"
    }
}
