package com.amerganim.banglakeyboard.data

import android.content.Context
import com.amerganim.banglakeyboard.engine.Suggester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Owns the [Suggester] and feeds it the bundled dictionary, the large word list,
 * and the user's learned data. Asset parsing happens off the main thread; the
 * built-in seed list inside [Suggester] makes it usable immediately even before
 * loading finishes.
 */
class DictionaryRepository(private val appContext: Context) {

    val suggester = Suggester()

    @Volatile
    var loaded: Boolean = false
        private set

    private val learnedFile: File
        get() = File(appContext.filesDir, LEARNED_FILE)

    /** Load dictionary + word list + learned data. Safe to call once at startup. */
    suspend fun load() = withContext(Dispatchers.IO) {
        if (loaded) return@withContext
        runCatching {
            appContext.assets.open(DICTIONARY_ASSET).bufferedReader().use {
                suggester.loadDictionaryData(it.readText())
            }
        }
        runCatching {
            appContext.assets.open(WORDS_ASSET).bufferedReader().use {
                suggester.loadWordList(it.readText())
            }
        }
        runCatching {
            if (learnedFile.exists()) suggester.loadLearnedData(learnedFile.readText())
        }
        loaded = true
    }

    /** Record a committed word and persist the updated learned counts. */
    suspend fun recordUsage(bangla: String) = withContext(Dispatchers.IO) {
        suggester.recordUsage(bangla)
        runCatching { learnedFile.writeText(suggester.dumpLearnedData()) }
        Unit
    }

    private companion object {
        const val DICTIONARY_ASSET = "dictionary.tsv"
        const val WORDS_ASSET = "words.tsv"
        const val LEARNED_FILE = "learned.tsv"
    }
}
