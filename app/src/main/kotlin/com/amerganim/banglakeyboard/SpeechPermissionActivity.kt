package com.amerganim.banglakeyboard

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Tiny transparent activity that requests the microphone permission on behalf of
 * the keyboard (an InputMethodService has no Activity context to request runtime
 * permissions itself). After granting, the user taps the 🎤 key again.
 */
class SpeechPermissionActivity : ComponentActivity() {

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val msg = if (granted) "Microphone enabled — tap 🎤 again" else "Microphone permission denied"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
