package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest

sealed class SearchResult
class ValidResult(val result: List<String>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()

class SearchViewModel(
    private val searchApi: SearchApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    @FlowPreview
    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryChannel
        .asFlow()
        .debounce(SEARCH_DELAY_MS)
        .mapLatest {
            try {
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
            } catch (e: CancellationException) {
                println("Search was cancelled!")
                throw e
            }
        }
        .catch { ErrorResult(it) }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()

    class Factory(private val assets: AssetManager, val dispatcher: CoroutineDispatcher) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
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
