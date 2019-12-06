package se.hellsoft.android.instantsearchdemo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.anyString
import kotlin.coroutines.ContinuationInterceptor

class SearchViewModelTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun testInstantSearch() = runBlocking {
        val searchApi = mock<SearchApi> {
            on { performSearch(anyString()) } doReturn listOf("aaa", "aab", "baa")
        }
        val viewModel = SearchViewModel(searchApi,
            Dispatchers.Unconfined
        )

        println("Start collecting")
        val collectJob = launch {
            viewModel.internalSearchResult.collect {
                println("Got search result: $it")

            }
        }

        println("Start searching")

            viewModel.queryChannel.send("aa")
            viewModel.queryChannel.send("bbb")
            viewModel.queryChannel.send("ccc")
            viewModel.queryChannel.send("dd")
        delay(600)
        println("advanceTimeBy")

        println("Done")

        viewModel.queryChannel.close()
//
        return@runBlocking
    }
}