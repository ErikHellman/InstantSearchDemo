package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest

sealed class SearchResult
class ValidResult(val result: List<String>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()
object TerminalError : SearchResult()

class SearchViewModel(
    private val searchApi: SearchApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    companion object {
        const val SEARCH_DELAY_MS = 500L
        const val MIN_QUERY_LENGTH = 3
    }

    @ExperimentalCoroutinesApi
    private val queryChannel = Channel<String>(Channel.CONFLATED)

    @FlowPreview
    @ExperimentalCoroutinesApi
    private val internalSearchResult = queryChannel
        .consumeAsFlow()
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
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    println("Search was cancelled!")
                    throw e
                } else {
                    ErrorResult(e)
                }
            }
        }
        .catch { it: Throwable -> emit(TerminalError) }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()

    @ExperimentalCoroutinesApi
    suspend fun onSearchTextChanged(text: String) = queryChannel.send(text)

    class Factory(private val assets: AssetManager, private val dispatcher: CoroutineDispatcher) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(SearchRepository(assets), dispatcher) as T
        }
    }
}
