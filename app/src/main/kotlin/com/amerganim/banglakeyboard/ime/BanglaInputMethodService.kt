package com.amerganim.banglakeyboard.ime

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amerganim.banglakeyboard.SpeechPermissionActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.amerganim.banglakeyboard.R
import com.amerganim.banglakeyboard.data.ClipboardHistory
import com.amerganim.banglakeyboard.data.DictionaryRepository
import com.amerganim.banglakeyboard.data.KeyboardPrefs
import com.amerganim.banglakeyboard.ui.KeyboardScreen
import com.amerganim.banglakeyboard.ui.SkinTones
import com.amerganim.banglakeyboard.ui.appLanguage
import com.amerganim.banglakeyboard.ui.localizedContext
import com.amerganim.banglakeyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The Android input method. Hosts the Compose keyboard inside the IME window.
 *
 * An [InputMethodService] is a plain [android.app.Service] with no lifecycle,
 * `ViewModelStore`, or `SavedStateRegistry`, but Compose's [ComposeView] requires
 * all three on its view tree. We implement them here and attach them to the input
 * view in [onCreateInputView] — this is the key glue that lets Compose run in an IME.
 */
class BanglaInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    KeyboardHost {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: DictionaryRepository
    private lateinit var prefs: KeyboardPrefs
    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        savedStateController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        repository = DictionaryRepository(applicationContext)
        prefs = KeyboardPrefs(applicationContext)
        // Start in sync, so only a clear requested while we are running triggers one.
        appliedClearGeneration = prefs.clearLearnedGeneration
        viewModel = KeyboardViewModel(
            connection = { currentInputConnection },
            repository = repository,
            scope = scope,
            onMicStart = ::onMicPressed,
            onMicStop = ::stopVoiceInput,
            onKeyFeedback = ::playKeyClick,
            onKeyHaptic = ::performKeyHaptic,
            host = this,
        )
        viewModel.updateRecentEmojis(prefs.recentEmojis)
        viewModel.updateEmojiTones(SkinTones.decode(prefs.emojiSkinTones))
        applyLookSettings()
        clickSoundId = soundPool.load(this, R.raw.key_click, 1)
        scope.launch { repository.load() }
        scope.launch { loadClipHistory() }
    }

    // ---- Clipboard history ------------------------------------------------

    private val clipboardManager: ClipboardManager? by lazy { getSystemService(ClipboardManager::class.java) }
    private val clipHistory = ClipboardHistory()
    private var clipListening = false
    private var fieldIsPassword = false

    // Nothing is captured until the saved history has loaded, or the load would
    // overwrite a clip captured in the meantime.
    private var clipsLoaded = false

    /**
     * Kept out of Auto Backup on purpose (noBackupFilesDir): copied text can be
     * personal, so it never leaves the device, not even in a cloud backup.
     */
    private val clipFile: File get() = File(noBackupFilesDir, "clipboard_history.tsv")

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { captureClip() }

    private suspend fun loadClipHistory() {
        val data = withContext(Dispatchers.IO) {
            runCatching { clipFile.takeIf { it.exists() }?.readText() }.getOrNull()
        }
        if (data != null) clipHistory.deserialize(data, System.currentTimeMillis())
        clipsLoaded = true
        applyClipboardSetting()
        captureClip()
    }

    /** Start/stop listening to match the setting; turning it off erases the history. */
    private fun applyClipboardSetting() {
        val enabled = prefs.clipboardHistory
        viewModel.updateClipboardEnabled(enabled)
        val cm = clipboardManager ?: return
        if (enabled && !clipListening) {
            cm.addPrimaryClipChangedListener(clipListener)
            clipListening = true
        } else if (!enabled) {
            if (clipListening) cm.removePrimaryClipChangedListener(clipListener)
            clipListening = false
            clipHistory.clearAll()
            scope.launch(Dispatchers.IO) { runCatching { clipFile.delete() } }
        }
        publishClips()
    }

    /**
     * Add the current system clip to the history if it is new text. Skips clips the
     * source app marked sensitive (passwords, OTPs, card numbers — Android 13+ sets
     * this flag) and anything copied while a password field is focused.
     */
    private fun captureClip() {
        if (!clipsLoaded || !prefs.clipboardHistory || fieldIsPassword) return
        val clip = runCatching { clipboardManager?.primaryClip }.getOrNull() ?: return
        val desc = clip.description
        val stamp = desc.timestamp
        if (stamp != 0L && stamp <= prefs.lastClipStamp) return
        prefs.lastClipStamp = stamp
        if (desc.extras?.getBoolean(EXTRA_IS_SENSITIVE, false) == true) return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).text?.toString() ?: return
        if (clipHistory.add(text, System.currentTimeMillis())) {
            saveClips()
            publishClips()
        }
    }

    private fun publishClips() {
        viewModel.updateClips(clipHistory.clips(System.currentTimeMillis()))
    }

    private fun saveClips() {
        val data = clipHistory.serialize()
        scope.launch(Dispatchers.IO) { runCatching { clipFile.writeText(data) } }
    }

    override fun readClipboard(): String? = runCatching {
        clipboardManager?.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.coerceToText(this)?.toString()
    }.getOrNull()

    override fun writeClipboard(text: String) {
        runCatching { clipboardManager?.setPrimaryClip(ClipData.newPlainText("text", text)) }
    }

    override fun refreshClips() {
        captureClip()
        publishClips()
    }

    override fun pinClip(text: String, pinned: Boolean) {
        clipHistory.setPinned(text, pinned, System.currentTimeMillis())
        saveClips()
        publishClips()
    }

    override fun removeClip(text: String) {
        clipHistory.remove(text)
        saveClips()
        publishClips()
    }

    override fun clearClips() {
        clipHistory.clearUnpinned()
        saveClips()
        publishClips()
    }

    override fun saveRecentEmojis(emojis: List<String>) {
        prefs.recentEmojis = emojis
    }

    override fun saveEmojiTones(tones: Map<String, String>) {
        prefs.emojiSkinTones = SkinTones.encode(tones)
    }

    /** A short message in the app's language (not the phone's system language). */
    private fun toast(resId: Int) {
        val text = localizedContext(this, appLanguage(this)).getString(resId)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    /** Language, theme and key borders, as chosen on the setup screen. */
    private fun applyLookSettings() {
        viewModel.updateUiLanguage(appLanguage(this))
        viewModel.updateTheme(KeyboardTheme.fromName(prefs.keyboardTheme))
        viewModel.updateKeyBorders(prefs.keyBorders)
    }

    override fun notice(resId: Int) {
        toast(resId)
    }

    // A bundled click played via SoundPool so it works regardless of the system's
    // "touch sounds" setting (which AudioManager.playSoundEffect depends on).
    private val soundPool: SoundPool by lazy {
        SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }
    private var clickSoundId = 0

    private fun playKeyClick() {
        if (clickSoundId == 0) clickSoundId = soundPool.load(this, R.raw.key_click, 1)
        soundPool.play(clickSoundId, 0.5f, 0.5f, 1, 0, 1f)
    }

    /**
     * Short vibration on key press. Unlike the click sound — which uses SoundPool to
     * bypass the system's touch-sounds setting — this deliberately respects the
     * system haptics setting, since the flag to override it is deprecated and is a
     * no-op on recent Android.
     */
    private fun performKeyHaptic() {
        inputView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    // ---- Voice typing ---------------------------------------------------

    private var speechRecognizer: SpeechRecognizer? = null

    private fun onMicPressed() {
        if (viewModel.listening) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(
                Intent(this, SpeechPermissionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            toast(R.string.speech_unavailable)
            return
        }
        startVoiceInput()
    }

    private fun startVoiceInput() {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer = recognizer
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { viewModel.updateListening(true) }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) viewModel.commitVoiceText(text.trim())
                finishVoiceInput()
            }

            override fun onError(error: Int) { finishVoiceInput() }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, viewModel.voiceLanguageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        runCatching { recognizer.startListening(intent) }
            .onFailure { finishVoiceInput() }
    }

    private fun stopVoiceInput() {
        runCatching { speechRecognizer?.stopListening() }
    }

    private fun finishVoiceInput() {
        viewModel.updateListening(false)
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
    }

    /** The Compose view hosting the keys — the target for haptic feedback. */
    private var inputView: View? = null

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                KeyboardScreen(viewModel)
            }
        }
        inputView = composeView
        // Compose creates its window recomposer by searching UP from the IME
        // window's root (decorView). The ViewTree owners must therefore live on
        // that root, not only on the deep-child ComposeView — otherwise the view
        // crashes on attach with "ViewTreeLifecycleOwner not found". We fall back
        // to the ComposeView itself if the window's decor isn't available yet.
        val root: View = window?.window?.decorView ?: composeView
        root.setViewTreeLifecycleOwner(this)
        root.setViewTreeViewModelStoreOwner(this)
        root.setViewTreeSavedStateRegistryOwner(this)
        return composeView
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        val password = isPasswordField(info)
        fieldIsPassword = password
        val noLearning = password || optsOutOfLearning(info)
        // Only fully hide suggestions for password fields. Other fields (e.g. a
        // search box that sets NO_SUGGESTIONS) still get suggestions; we just
        // don't learn from them.
        viewModel.onInputStart(
            noLearning = noLearning,
            noSuggestions = password,
            fieldInputType = info?.inputType ?: 0,
        )
        viewModel.updateImeAction(editorAction(info))
    }

    /**
     * The action the Enter key should perform for this field (Search/Done/Next/…),
     * or [EditorInfo.IME_ACTION_NONE] when Enter should just insert a newline.
     *
     * A declared action wins even on a multi-line field (e.g. Google's search bar is
     * multi-line yet wants Search); only [EditorInfo.IME_FLAG_NO_ENTER_ACTION] — which
     * the framework sets for fields that truly want a newline — suppresses it.
     */
    private fun editorAction(info: EditorInfo?): Int {
        if (info == null) return EditorInfo.IME_ACTION_NONE
        if ((info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return EditorInfo.IME_ACTION_NONE
        }
        return when (val action = info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_PREVIOUS -> action
            else -> EditorInfo.IME_ACTION_NONE // NONE / UNSPECIFIED → newline
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd,
        )
        viewModel.onSelectionChanged(newSelStart, newSelEnd, candidatesStart, candidatesEnd)
    }

    /** Password fields — hide suggestions and never learn. */
    private fun isPasswordField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val type = info.inputType
        val variation = type and InputType.TYPE_MASK_VARIATION
        return when (type and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT -> variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    /** Fields that don't want their content learned/saved (suggestions still OK). */
    private fun optsOutOfLearning(info: EditorInfo?): Boolean {
        if (info == null) return false
        val noSuggestions = (info.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0
        val noLearning = (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        return noSuggestions || noLearning
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Pick up any settings changes made in the setup screen.
        viewModel.updateKeySize(prefs.keySize)
        viewModel.updateKeySound(prefs.keySound)
        viewModel.updateKeyHaptic(prefs.keyHaptic)
        viewModel.updateSmartConjunct(prefs.smartConjunct)
        applyLookSettings()
        applyPendingLearnedClear()
        applyClipboardSetting()
        captureClip() // something may have been copied while the keyboard was idle
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * The settings screen deletes the learned files and bumps a generation counter.
     * A keyboard that was already running still holds the data in memory and would
     * write it straight back on the next commit, so drop it here before any typing
     * happens.
     */
    private fun applyPendingLearnedClear() {
        val generation = prefs.clearLearnedGeneration
        if (generation == appliedClearGeneration) return
        appliedClearGeneration = generation
        scope.launch { repository.clearLearned() }
    }

    /**
     * Seeded from prefs in [onCreate], never -1 — a sentinel would make the first
     * [onStartInputView] of every service start look like a pending clear and wipe
     * the user's learned words.
     */
    private var appliedClearGeneration: Int = 0

    private companion object {
        /** ClipDescription.EXTRA_IS_SENSITIVE (API 33), spelled out for older SDKs. */
        const val EXTRA_IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        finishVoiceInput()
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        if (clipListening) clipboardManager?.removePrimaryClipChangedListener(clipListener)
        finishVoiceInput()
        soundPool.release()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        scope.cancel()
        super.onDestroy()
    }
}
