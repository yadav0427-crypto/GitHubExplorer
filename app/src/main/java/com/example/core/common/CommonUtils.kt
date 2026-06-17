package com.example.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtils {

    fun formatNumber(number: Int?): String {
        if (number == null) return "0"
        return when {
            number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format(Locale.US, "%.1fk", number / 1_000.0)
            else -> number.toString()
        }
    }

    fun formatDateISO(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString) ?: return isoString
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
            formatter.format(date)
        } catch (e: Exception) {
            try {
                // Return just the date part if it is simplified
                if (isoString.length >= 10) isoString.substring(0, 10) else isoString
            } catch (ex: Exception) {
                isoString
            }
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}
