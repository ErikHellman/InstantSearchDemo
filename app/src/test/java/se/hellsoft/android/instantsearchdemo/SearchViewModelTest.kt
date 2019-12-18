package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.anyString

class SearchViewModelTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun testInstantSearch() = runBlockingTest {
//        val searchApi = mock<SearchApi> {
//            on { performSearch(anyString()) } doReturn listOf("aaa", "aab", "baa")
//        }
        val assets = mock<AssetManager> {
            on { open(anyString()) } doReturn ClassLoader.getSystemResourceAsStream("words_alpha.txt")
        }

        val viewModel = SearchViewModel(
            SearchRepository(assets),
            Dispatchers.Unconfined
        )

        println("Start collecting")
//        launch {
//        }

        println("Start searching")
        launch {
            viewModel.internalSearchResult.collect {
                println("Collected $it")
                assertTrue(it is ValidResult)
                it as ValidResult
                assertEquals(listOf("antitheses"), it.result)
            }
        }



        viewModel.queryChannel.send("")
        viewModel.queryChannel.send("an")
        viewModel.queryChannel.send("ant")
        viewModel.queryChannel.send("anti")
        advanceTimeBy(600)

        viewModel.queryChannel.send("antit")
        viewModel.queryChannel.send("antith")
        viewModel.queryChannel.send("antithe")
        viewModel.queryChannel.send("antithes")
        viewModel.queryChannel.send("antithese")
        viewModel.queryChannel.send("antitheses")

        advanceTimeBy(600)

        viewModel.queryChannel.close()

        cleanupTestCoroutines()

        return@runBlockingTest
    }
}