package com.amerganim.banglakeyboard.ime

import android.Manifest
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
import com.amerganim.banglakeyboard.data.DictionaryRepository
import com.amerganim.banglakeyboard.data.KeyboardPrefs
import com.amerganim.banglakeyboard.ui.KeyboardScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
    SavedStateRegistryOwner {

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
        viewModel = KeyboardViewModel(
            connection = { currentInputConnection },
            repository = repository,
            scope = scope,
            onMicStart = ::onMicPressed,
            onMicStop = ::stopVoiceInput,
            onKeyFeedback = ::playKeyClick,
        )
        clickSoundId = soundPool.load(this, R.raw.key_click, 1)
        scope.launch { repository.load() }
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
            Toast.makeText(this, R.string.speech_unavailable, Toast.LENGTH_SHORT).show()
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

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                KeyboardScreen(viewModel)
            }
        }
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
        viewModel.updateSmartConjunct(prefs.smartConjunct)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        finishVoiceInput()
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        finishVoiceInput()
        soundPool.release()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        scope.cancel()
        super.onDestroy()
    }
}
