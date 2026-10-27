package com.notasapp.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateFormatter {

    fun formatIso(isoDate: String): String {
        if (isoDate.length < 19) return isoDate
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoDate.substring(0, 19)) ?: return isoDate
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            isoDate
        }
    }
}
