package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.UnknownHostException

interface SearchApi {
    suspend fun performSearch(query: String): List<String>
}

class SearchRepository(private val assets: AssetManager,
                       private val maxResult: Int = DEFAULT_RESULT_MAX_SIZE) : SearchApi {
    companion object {
        private const val DEFAULT_RESULT_MAX_SIZE = 250
    }

    override suspend fun performSearch(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            println("Search for $query")
            val inputStream = assets.open("words_alpha.txt")
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            bufferedReader.use { reader: BufferedReader ->
                reader.lineSequence()
                    .filter { it.contains(query, true) }
                    .take(maxResult)
                    .toList()
            }
        }
    }
}