package com.amerganim.banglakeyboard.data

import android.content.Context
import com.amerganim.banglakeyboard.engine.Bigrams
import com.amerganim.banglakeyboard.engine.Conjuncts
import com.amerganim.banglakeyboard.engine.EnglishSuggester
import com.amerganim.banglakeyboard.engine.Suggester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Which language a suggestion/word belongs to. */
enum class Lang { BANGLA, ENGLISH }

/**
 * Owns the Bangla and English suggesters plus their personal next-word models, and
 * persists everything the user teaches the keyboard (learned counts, saved words,
 * and bigrams). Asset parsing and all writes happen off the main thread; reads
 * ([suggestBangla], [suggestEnglish], [predictNext]) are lock-free snapshots.
 */
class DictionaryRepository(private val appContext: Context) {

    private val bangla = Suggester()
    private val english = EnglishSuggester()
    private val banglaBigrams = Bigrams()
    private val englishBigrams = Bigrams()

    private val bnUserWords = LinkedHashSet<String>()
    private val enUserWords = LinkedHashSet<String>()

    private val writeMutex = Mutex()

    @Volatile
    var loaded: Boolean = false
        private set

    private fun file(name: String) = File(appContext.filesDir, name)

    suspend fun load() = withContext(Dispatchers.IO) {
        if (loaded) return@withContext
        runCatching { appContext.assets.open(BN_DICT).bufferedReader().use { bangla.loadDictionaryData(it.readText()) } }
        runCatching { appContext.assets.open(BN_WORDS).bufferedReader().use { bangla.loadWordList(it.readText()) } }
        runCatching { appContext.assets.open(EN_WORDS).bufferedReader().use { english.loadWordList(it.readText()) } }
        runCatching {
            appContext.assets.open(JUKTAKKHOR).bufferedReader().use { Conjuncts.setFromList(it.readText()) }
        }

        runCatching { file(BN_LEARNED).takeIf { it.exists() }?.let { bangla.loadLearnedData(it.readText()) } }
        runCatching { file(EN_LEARNED).takeIf { it.exists() }?.let { english.loadLearnedData(it.readText()) } }
        runCatching { file(BN_BIGRAMS).takeIf { it.exists() }?.let { banglaBigrams.load(it.readText()) } }
        runCatching { file(EN_BIGRAMS).takeIf { it.exists() }?.let { englishBigrams.load(it.readText()) } }
        runCatching {
            file(BN_USER).takeIf { it.exists() }?.readLines()?.forEach {
                val w = it.trim(); if (w.isNotEmpty()) { bnUserWords.add(w); bangla.addUserWord(w) }
            }
        }
        runCatching {
            file(EN_USER).takeIf { it.exists() }?.readLines()?.forEach {
                val w = it.trim(); if (w.isNotEmpty()) { enUserWords.add(w); english.addUserWord(w) }
            }
        }
        loaded = true
    }

    // ---- Reads (main thread, lock-free) -------------------------------------

    fun suggestBangla(prefix: String, smart: Boolean = false): List<String> = bangla.suggest(prefix, smart)
    fun suggestEnglish(prefix: String): List<String> = english.suggest(prefix)

    /** Completions for an already-formed Bangla string (Amader layout). */
    fun suggestBanglaByText(banglaPrefix: String): List<String> = bangla.suggestByBangla(banglaPrefix)

    fun predictNext(lang: Lang, prevWord: String): List<String> {
        if (prevWord.isBlank()) return emptyList()
        return when (lang) {
            Lang.BANGLA -> banglaBigrams.predict(prevWord)
            Lang.ENGLISH -> englishBigrams.predict(prevWord)
        }
    }

    // ---- Writes (background) ------------------------------------------------

    /** Record a committed [word]; learn it, save it, and link it after [prevWord]. */
    suspend fun commitWord(lang: Lang, prevWord: String, word: String) = withContext(Dispatchers.IO) {
        if (word.isBlank()) return@withContext
        writeMutex.withLock {
            when (lang) {
                Lang.BANGLA -> {
                    bangla.recordUsage(word)
                    if (bnUserWords.add(word)) bangla.addUserWord(word)
                    banglaBigrams.record(prevWord, word)
                    persistBangla()
                }
                Lang.ENGLISH -> {
                    english.recordUsage(word)
                    if (enUserWords.add(word.lowercase())) english.addUserWord(word)
                    englishBigrams.record(prevWord.lowercase(), word.lowercase())
                    persistEnglish()
                }
            }
        }
    }

    /** Forget [word] from suggestions and predictions. */
    suspend fun forget(lang: Lang, word: String) = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            when (lang) {
                Lang.BANGLA -> {
                    bangla.forget(word); banglaBigrams.forget(word); bnUserWords.remove(word); persistBangla()
                }
                Lang.ENGLISH -> {
                    val w = word.lowercase()
                    english.forget(w); englishBigrams.forget(w); enUserWords.remove(w); persistEnglish()
                }
            }
        }
    }

    /**
     * Forget everything the user has taught the keyboard — learned counts, saved
     * words and next-word pairs, in memory and on disk. The bundled dictionaries
     * are untouched, so ordinary suggestions keep working.
     */
    suspend fun clearLearned() = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            bangla.clearLearned()
            english.clearLearned()
            banglaBigrams.clear()
            englishBigrams.clear()
            bnUserWords.clear()
            enUserWords.clear()
            deleteStoredFiles(appContext)
        }
    }

    private fun persistBangla() {
        runCatching { file(BN_LEARNED).writeText(bangla.dumpLearnedData()) }
        runCatching { file(BN_BIGRAMS).writeText(banglaBigrams.dump()) }
        runCatching { file(BN_USER).writeText(bnUserWords.joinToString("\n")) }
    }

    private fun persistEnglish() {
        runCatching { file(EN_LEARNED).writeText(english.dumpLearnedData()) }
        runCatching { file(EN_BIGRAMS).writeText(englishBigrams.dump()) }
        runCatching { file(EN_USER).writeText(enUserWords.joinToString("\n")) }
    }

    companion object {
        private const val BN_DICT = "dictionary.tsv"
        private const val BN_WORDS = "words.tsv"
        private const val EN_WORDS = "english_words.txt"
        private const val JUKTAKKHOR = "juktakkhor.txt"
        private const val BN_LEARNED = "bn_learned.tsv"
        private const val EN_LEARNED = "en_learned.tsv"
        private const val BN_BIGRAMS = "bn_bigrams.tsv"
        private const val EN_BIGRAMS = "en_bigrams.tsv"
        private const val BN_USER = "bn_user.tsv"
        private const val EN_USER = "en_user.tsv"

        /** Every file written by [persistBangla] / [persistEnglish]. */
        private val STORED_FILES = listOf(
            BN_LEARNED, EN_LEARNED, BN_BIGRAMS, EN_BIGRAMS, BN_USER, EN_USER,
        )

        /**
         * Delete the on-disk learned data. Exposed so the settings screen can wipe
         * it immediately, without owning a loaded repository of its own; a running
         * keyboard drops its in-memory copy separately (see [clearLearned]).
         */
        fun deleteStoredFiles(context: Context) {
            val dir = context.applicationContext.filesDir
            for (name in STORED_FILES) runCatching { File(dir, name).delete() }
        }
    }
}
