package se.hellsoft.android.instantsearchdemo

import android.content.res.AssetManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
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

        // TODO Write test..

        return@runBlocking
    }
}