package se.hellsoft.android.instantsearchdemo

import android.app.Application
import android.content.res.AssetManager
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
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

class SearchViewModel(private val searchApi: SearchApi) : ViewModel() {
    @VisibleForTesting
    internal val queryChannel = Channel<String>()
    @ExperimentalCoroutinesApi
    private val queryFlow = queryChannel.consumeAsFlow().conflate()

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryFlow
        .mapLatest {
            try {
                delay(SEARCH_DELAY_MS)
                if (it.length >= MIN_QUERY_LENGTH) {
                    val searchResult = searchApi.performSearch(it)
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

    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()

    fun search(text: String) {
        viewModelScope.launch {
            queryChannel.send(text)
        }
    }

    class Factory(private val assets: AssetManager) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(SearchRepository(assets)) as T
        }
    }

    companion object{
        private const val TAG = "SearchViewModel"
        const val SEARCH_DELAY_MS = 500L
        const val MIN_QUERY_LENGTH = 3
    }
}