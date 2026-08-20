package com.example.animekai.offline

data class OfflinePage(
    val id: String,
    val url: String,
    val title: String,
    val timestamp: Long,
    val filePath: String,
    val fileSizeBytes: Long
) {
    fun getFormattedSize(): String {
        return when {
            fileSizeBytes < 1024 -> "$fileSizeBytes B"
            fileSizeBytes < 1024 * 1024 -> String.format("%.1f KB", fileSizeBytes / 1024.0)
            else -> String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0))
        }
    }

    fun getFormattedDate(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
        }
    }
}
