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

    private companion object {
        const val BN_DICT = "dictionary.tsv"
        const val BN_WORDS = "words.tsv"
        const val EN_WORDS = "english_words.txt"
        const val JUKTAKKHOR = "juktakkhor.txt"
        const val BN_LEARNED = "bn_learned.tsv"
        const val EN_LEARNED = "en_learned.tsv"
        const val BN_BIGRAMS = "bn_bigrams.tsv"
        const val EN_BIGRAMS = "en_bigrams.tsv"
        const val BN_USER = "bn_user.tsv"
        const val EN_USER = "en_user.tsv"
    }
}
