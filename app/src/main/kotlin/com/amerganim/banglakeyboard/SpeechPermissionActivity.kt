package com.amerganim.banglakeyboard

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.amerganim.banglakeyboard.ui.appLanguage
import com.amerganim.banglakeyboard.ui.localizedContext

/**
 * Tiny transparent activity that requests the microphone permission on behalf of
 * the keyboard (an InputMethodService has no Activity context to request runtime
 * permissions itself). After granting, the user taps the 🎤 key again.
 */
class SpeechPermissionActivity : ComponentActivity() {

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val msg = if (granted) R.string.mic_enabled else R.string.mic_denied
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localizedContext(newBase, appLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
