package se.hellsoft.android.instantsearchdemo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@FlowPreview
@ExperimentalCoroutinesApi
class SearchViewModelTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    val mainDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInstantSearch() = mainDispatcher.runBlockingTest {
        // GIVEN
        val fakeApi = FakeApi()
        val expectedQueries = listOf("aa", "bbb", "ccc", "ddd actual query")

        val subject = SearchViewModel(
            fakeApi,
            mainDispatcher
        )

        // start collecting flows in a new coroutine
        val collectParent = launch {
            // observe the livedata to collect the flow to trigger the debouncing behavior
            subject.searchResult.observeForever {}
        }

        // WHEN
        for (query in expectedQueries) {
            subject.onSearchTextChanged(query)
            advanceTimeBy(35) // make sure a small time advance still keeps debouncing
        }

        // actually trigger the debounce delay
        advanceTimeBy(500)

        // need to cancel all the coroutines launched for collection
        collectParent.cancel()

        // THEN
        assert(fakeApi.actualQueries == listOf("ddd actual query")) { "Only saw one search" }
    }

    class FakeApi : SearchApi {
        val actualQueries = mutableListOf<String>()

        override suspend fun performSearch(query: String): List<String> {
            actualQueries.add(query)
            return listOf()
        }
    }
}