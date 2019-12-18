package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*

sealed class SearchResult
class ValidResult(val result: List<String>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()

class SearchViewModel(
    private val searchApi: SearchApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    @VisibleForTesting
    internal val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryChannel
        .asFlow()
        .debounce(SEARCH_DELAY_MS)
        .mapLatest {
            if (it.length >= MIN_QUERY_LENGTH) {
                val searchResult = withContext(ioDispatcher) {
                    searchApi.performSearch(it)
                }
                println("Search result: ${searchResult.size} hits")

                if (searchResult.isNotEmpty()) {
                    ValidResult(searchResult)
                } else {
                    EmptyResult
                }
            } else {
                EmptyQuery
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

    class Factory(private val assets: AssetManager, val dispatcher: CoroutineDispatcher) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(SearchRepository(assets)) as T
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
        const val SEARCH_DELAY_MS = 500L
        const val MIN_QUERY_LENGTH = 3
    }
}

internal fun <T> ReceiveChannel<T>.receiveAsFlow() = flow {
    for (item in this@receiveAsFlow) {
        emit(item)
    }
}