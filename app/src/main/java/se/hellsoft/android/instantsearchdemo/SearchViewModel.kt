package se.hellsoft.android.instantsearchdemo

import android.app.Application
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class SearchResult
class ValidResult(val result: List<String>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    @VisibleForTesting
    internal val queryChannel = Channel<String>()
    @ExperimentalCoroutinesApi
    private val queryFlow = queryChannel
        .consumeAsFlow().conflate()

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryFlow
        .mapLatest {
            try {
                delay(500)
                if (it.length >= MIN_QUERY_LENGTH) {
                    val searchResult = performSearch(it)
                    Log.d(TAG, "Search result: ${searchResult.size} hits")

                    if (searchResult.isNotEmpty()) {
                        ValidResult(searchResult)
                    } else {
                        EmptyResult
                    }
                } else {
                    EmptyQuery
                }
            } catch (e: CancellationException) {
                Log.w(TAG,"mapLatest got cancelled!")
                throw e
            }
        }
        .catch { ErrorResult(it) }

    private suspend fun performSearch(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            val inputStream = getApplication<Application>().assets.open("words_alpha.txt")
            val lineSequence = BufferedReader(InputStreamReader(inputStream)).lineSequence()
            lineSequence.filter { it.contains(query, true) }.toList()
        }
    }

    val test = queryChannel.consumeAsFlow()

    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData(viewModelScope.coroutineContext)

    fun search(text: String) {
        viewModelScope.launch {
            queryChannel.send(text)
        }
    }

    companion object{
        private const val TAG = "SearchViewModel"
        const val SEARCH_DELAY = 200L
        const val MIN_QUERY_LENGTH = 3

        private fun <T> ReceiveChannel<T>.receiveAsFlow() = flow {
            for (item in this@receiveAsFlow) {
                emit(item)
            }
        }

    }
}