package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.UnknownHostException
import kotlin.random.Random

interface SearchApi {
    suspend fun performSearch(query: String): List<String>
}

class SearchRepository(private val assets: AssetManager,
                       private val maxResult: Int = DEFAULT_RESULT_MAX_SIZE) : SearchApi {
    companion object {
        private const val DEFAULT_RESULT_MAX_SIZE = 250
        private const val RANDOM_ERROR_THRESHOLD = 0.75
    }

    override suspend fun performSearch(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            // This is for creating some random, fake network errors...
            if (Random.nextFloat() > RANDOM_ERROR_THRESHOLD) {
                println("Random error thrown!")
                throw IOException("This is a random network error!")
            }
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