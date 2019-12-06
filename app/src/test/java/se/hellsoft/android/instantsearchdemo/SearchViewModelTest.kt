package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class SearchViewModelTest {

    @Test
    fun testInstantSearch() = runBlocking {
        val assets = mock<AssetManager> {
            on { open(anyString()) } doReturn ClassLoader.getSystemResourceAsStream("words_alpha.txt")
        }
        val searchRepository = SearchRepository(assets)
        val viewModel = SearchViewModel(searchRepository)

        viewModel.internalSearchResult.collect {
            when(it) {
                is ValidResult -> assertEquals(1, it.result.size)
            }
        }

        viewModel.search("Antitheses")


        delay(1000)

        viewModel.queryChannel.close()

        return@runBlocking 
    }
}