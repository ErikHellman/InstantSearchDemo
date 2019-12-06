package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader

interface SearchApi {
    fun performSearch(query: String): List<String>
}

class SearchRepository(private val assets: AssetManager) : SearchApi {
    override fun performSearch(query: String): List<String> {
        println("Search for $query")
        val inputStream = assets.open("words_alpha.txt")
        val lineSequence = BufferedReader(InputStreamReader(inputStream)).lineSequence()
        return lineSequence.filter { it.contains(query, true) }.toList()
    }
}