package se.hellsoft.android.instantsearchdemo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

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
        val actualQueries = mutableListOf<String>()
        val expectedQueries = listOf("aa", "bbb", "ccc", "ddd actual query")

        val subject = SearchViewModel(
            fakeApi,
            mainDispatcher
        )

        // start collecting flows in a new coroutine
        val collectParent = launch {
            // collect the flow to trigger the debouncing behavior
            subject.internalSearchResult.launchIn(this)

            // make sure we're actually sending all queries through – since we're modifying
            // execution order with TestCoroutineDispatcher. This is just a sanity check.
            subject.queryChannel.asFlow().mapLatest { query ->
                actualQueries.add(query)
            }.launchIn(this)
        }

        // WHEN
        for (query in expectedQueries) {
            subject.queryChannel.send(query)
            advanceTimeBy(35) // make sure a small time advance still keeps debouncing
        }

        // actually trigger the debounce delay
        advanceTimeBy(500)

        // need to cancel all the coroutines launched for collection
        collectParent.cancel()

        // THEN
        assert(fakeApi.actualQueries == listOf("ddd actual query")) { "Only saw one search" }
        assert(actualQueries == expectedQueries) { "all queries were sent, then debounced" }
    }

    class FakeApi: SearchApi {
        val actualQueries = mutableListOf<String>()

        override fun performSearch(query: String): List<String> {
            actualQueries.add(query)
            return listOf()
        }

    }
}