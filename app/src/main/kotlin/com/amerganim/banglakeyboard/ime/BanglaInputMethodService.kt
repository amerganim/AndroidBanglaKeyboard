package com.amerganim.banglakeyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.amerganim.banglakeyboard.data.DictionaryRepository
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
    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        savedStateController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        repository = DictionaryRepository(applicationContext)
        viewModel = KeyboardViewModel(
            connection = { currentInputConnection },
            repository = repository,
            scope = scope,
        )
        scope.launch { repository.load() }
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
        viewModel.onInputStart()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        scope.cancel()
        super.onDestroy()
    }
}
