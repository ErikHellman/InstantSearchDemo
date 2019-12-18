package se.hellsoft.android.instantsearchdemo

import kotlinx.coroutines.delay

data class BookModel(val bookId: Int) {
    companion object {
//        suspend operator fun invoke(bookId: Int): BookModel? {
//            delay(1000)
//            return fetchBookModel(bookId)
//        }

        suspend fun fetchBookModel(bookId: Int): BookModel? {
            return null
        }
    }
}

suspend fun testBookModel() {
    val book: BookModel? = BookModel(123)
}
