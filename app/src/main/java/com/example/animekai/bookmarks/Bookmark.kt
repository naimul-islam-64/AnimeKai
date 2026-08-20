package com.example.animekai.bookmarks

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Bookmark(
    val id: String,
    val title: String,
    val url: String,
    val category: String = "Anime",
    val isDefault: Boolean = false
)

class BookmarkManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("animekai_bookmark_prefs", Context.MODE_PRIVATE)

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        val jsonStr = prefs.getString(KEY_BOOKMARKS, null)
        if (jsonStr == null) {
            val defaults = getDefaultBookmarks()
            _bookmarks.value = defaults
            saveBookmarks(defaults)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<Bookmark>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Bookmark(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            url = obj.getString("url"),
                            category = obj.optString("category", "Anime"),
                            isDefault = obj.optBoolean("isDefault", false)
                        )
                    )
                }
                _bookmarks.value = list
            } catch (e: Exception) {
                _bookmarks.value = getDefaultBookmarks()
            }
        }
    }

    private fun saveBookmarks(list: List<Bookmark>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("url", item.url)
                put("category", item.category)
                put("isDefault", item.isDefault)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
        _bookmarks.value = list
    }

    fun addBookmark(title: String, url: String, category: String = "Favorite") {
        if (url.isBlank()) return
        val current = _bookmarks.value.toMutableList()
        if (current.none { it.url == url }) {
            current.add(
                0,
                Bookmark(
                    id = UUID.randomUUID().toString().take(8),
                    title = title.ifBlank { "Anime Page" },
                    url = url,
                    category = category
                )
            )
            saveBookmarks(current)
        }
    }

    fun removeBookmark(id: String) {
        val current = _bookmarks.value.filterNot { it.id == id }
        saveBookmarks(current)
    }

    fun isBookmarked(url: String): Boolean {
        return _bookmarks.value.any { it.url == url }
    }

    private fun getDefaultBookmarks(): List<Bookmark> = listOf(
        Bookmark("def_1", "Home Portal", "https://animekai.be/home", "Navigation", true),
        Bookmark("def_2", "Trending Anime", "https://animekai.be/filter?sort=trending", "Featured", true),
        Bookmark("def_3", "Anime Movies", "https://animekai.be/filter?type=movie", "Movies", true),
        Bookmark("def_4", "Release Schedule", "https://animekai.be/schedule", "Schedule", true),
        Bookmark("def_5", "Genre Directory", "https://animekai.be/filter", "Directory", true)
    )

    companion object {
        private const val KEY_BOOKMARKS = "key_animekai_bookmarks"
    }
}
