package com.example.animekai.offline

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class OfflinePageManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("animekai_offline_prefs", Context.MODE_PRIVATE)

    private val offlineDir: File = File(context.filesDir, "offline_pages").apply {
        if (!exists()) mkdirs()
    }

    private val _offlinePages = MutableStateFlow<List<OfflinePage>>(emptyList())
    val offlinePages: StateFlow<List<OfflinePage>> = _offlinePages.asStateFlow()

    init {
        loadPagesFromPrefs()
    }

    private fun loadPagesFromPrefs() {
        val jsonStr = prefs.getString(KEY_OFFLINE_PAGES, "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<OfflinePage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val file = File(obj.getString("filePath"))
                if (file.exists()) {
                    list.add(
                        OfflinePage(
                            id = obj.getString("id"),
                            url = obj.getString("url"),
                            title = obj.optString("title", "AnimeKai Page"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            filePath = file.absolutePath,
                            fileSizeBytes = file.length()
                        )
                    )
                }
            }
            // Sort newest first
            val sorted = list.sortedByDescending { it.timestamp }
            _offlinePages.value = sorted
            persistPages(sorted)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load offline pages", e)
        }
    }

    private fun persistPages(pages: List<OfflinePage>) {
        try {
            val jsonArray = JSONArray()
            for (page in pages) {
                val obj = JSONObject().apply {
                    put("id", page.id)
                    put("url", page.url)
                    put("title", page.title)
                    put("timestamp", page.timestamp)
                    put("filePath", page.filePath)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_OFFLINE_PAGES, jsonArray.toString()).apply()
            _offlinePages.value = pages
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist offline pages", e)
        }
    }

    /**
     * Automatically saves current page archive.
     * Enforces the MAX 10 cached pages rule.
     */
    fun savePage(webView: WebView, url: String, title: String) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("file:")) {
            return
        }

        try {
            val pageId = UUID.randomUUID().toString().take(8)
            val safeFileName = "page_${System.currentTimeMillis()}_$pageId.mht"
            val targetFile = File(offlineDir, safeFileName)

            val displayTitle = if (title.isNotBlank() && !title.startsWith("http")) {
                title
            } else {
                deriveTitleFromUrl(url)
            }

            webView.saveWebArchive(targetFile.absolutePath, false) { savedPath ->
                if (savedPath != null) {
                    val savedFile = File(savedPath)
                    if (savedFile.exists() && savedFile.length() > 0) {
                        Log.d(TAG, "Page saved successfully to $savedPath, size: ${savedFile.length()}")

                        val currentList = _offlinePages.value.toMutableList()

                        // Remove existing entry with same URL if already cached to avoid duplicates
                        val existingIndex = currentList.indexOfFirst { it.url == url }
                        if (existingIndex != -1) {
                            val oldFile = File(currentList[existingIndex].filePath)
                            if (oldFile.exists()) oldFile.delete()
                            currentList.removeAt(existingIndex)
                        }

                        val newPage = OfflinePage(
                            id = pageId,
                            url = url,
                            title = displayTitle,
                            timestamp = System.currentTimeMillis(),
                            filePath = savedPath,
                            fileSizeBytes = savedFile.length()
                        )

                        // Insert at top
                        currentList.add(0, newPage)

                        // Enforce strictly the last 10 pages limit
                        while (currentList.size > MAX_OFFLINE_PAGES) {
                            val removed = currentList.removeAt(currentList.lastIndex)
                            val fileToDelete = File(removed.filePath)
                            if (fileToDelete.exists()) {
                                fileToDelete.delete()
                                Log.d(TAG, "Pruned oldest offline page: ${removed.title}")
                            }
                        }

                        persistPages(currentList)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving web archive for $url", e)
        }
    }

    fun deletePage(pageId: String) {
        val currentList = _offlinePages.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == pageId }
        if (index != -1) {
            val removed = currentList.removeAt(index)
            val file = File(removed.filePath)
            if (file.exists()) file.delete()
            persistPages(currentList)
        }
    }

    fun clearAll() {
        val currentList = _offlinePages.value
        for (page in currentList) {
            val file = File(page.filePath)
            if (file.exists()) file.delete()
        }
        persistPages(emptyList())
    }

    fun getOfflineVersionForUrl(url: String): OfflinePage? {
        return _offlinePages.value.firstOrNull { it.url.equals(url, ignoreCase = true) }
    }

    private fun deriveTitleFromUrl(url: String): String {
        return try {
            val lastSegment = url.trimEnd('/').substringAfterLast('/')
            if (lastSegment.isNotBlank()) {
                lastSegment.replace('-', ' ').replace('_', ' ').replaceFirstChar { it.uppercase() }
            } else {
                "AnimeKai Page"
            }
        } catch (e: Exception) {
            "AnimeKai Page"
        }
    }

    companion object {
        private const val TAG = "OfflinePageManager"
        private const val KEY_OFFLINE_PAGES = "key_offline_pages_json"
        const val MAX_OFFLINE_PAGES = 10
    }
}
