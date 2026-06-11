package com.amerganim.banglakeyboard

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.amerganim.banglakeyboard.data.KeySize
import com.amerganim.banglakeyboard.data.KeyboardPrefs
import com.amerganim.banglakeyboard.ui.AmaderGuideScreen
import com.amerganim.banglakeyboard.ui.GuideScreen
import com.amerganim.banglakeyboard.ui.PolicyScreen
import com.amerganim.banglakeyboard.ui.theme.BanglaKeyboardTheme
import java.util.Locale

private enum class Screen { SETUP, GUIDE, AMADER_GUIDE, POLICY }

/**
 * Launcher screen: enable/select the keyboard, choose the app language (EN/বাংলা),
 * pick a key size and other settings, open the guides, and try it out.
 */
class SetupActivity : ComponentActivity() {

    /** Apply the UI language (Bangla by default) to this activity's resources. */
    override fun attachBaseContext(newBase: Context) {
        val lang = KeyboardPrefs(newBase).appLang.ifEmpty { "bn" } // Bangla default
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale(lang))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BanglaKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var screen by remember { mutableStateOf(Screen.SETUP) }
                    // Hoisted above the screen switch so it survives navigating to a
                    // guide/policy and back (and across a language change).
                    var tryText by rememberSaveable { mutableStateOf("") }
                    when (screen) {
                        Screen.GUIDE -> {
                            BackHandler { screen = Screen.SETUP }
                            GuideScreen(onBack = { screen = Screen.SETUP })
                        }
                        Screen.AMADER_GUIDE -> {
                            BackHandler { screen = Screen.SETUP }
                            AmaderGuideScreen(onBack = { screen = Screen.SETUP })
                        }
                        Screen.POLICY -> {
                            BackHandler { screen = Screen.SETUP }
                            PolicyScreen(onBack = { screen = Screen.SETUP })
                        }
                        Screen.SETUP -> SetupScreen(
                            tryText = tryText,
                            onTryTextChange = { tryText = it },
                            onOpenGuide = { screen = Screen.GUIDE },
                            onOpenAmaderGuide = { screen = Screen.AMADER_GUIDE },
                            onOpenPolicy = { screen = Screen.POLICY },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(
    tryText: String,
    onTryTextChange: (String) -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAmaderGuide: () -> Unit,
    onOpenPolicy: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { KeyboardPrefs(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Language (EN / বাংলা)
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        val effectiveLang = prefs.appLang.ifEmpty { "bn" } // Bangla default
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            val langs = listOf("en" to "English", "bn" to "বাংলা")
            langs.forEachIndexed { index, (code, label) ->
                SegmentedButton(
                    selected = effectiveLang == code,
                    onClick = {
                        if (effectiveLang != code) {
                            prefs.appLang = code
                            context.findActivity()?.recreate()
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, langs.size),
                ) { Text(label) }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.getting_started), fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.gs_1))
                Text(stringResource(R.string.gs_2))
                Text(stringResource(R.string.gs_3))
                Text(stringResource(R.string.gs_4))
            }
        }

        Button(
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.enable_keyboard)) }

        FilledTonalButton(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.choose_keyboard)) }

        OutlinedButton(onClick = onOpenGuide, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bangla_typing_guide))
        }
        OutlinedButton(onClick = onOpenAmaderGuide, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.amader_layout_guide))
        }
        OutlinedButton(onClick = onOpenPolicy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.privacy_policy))
        }

        Text(stringResource(R.string.key_size), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        var keySize by remember { mutableStateOf(prefs.keySize) }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            KeySize.entries.forEachIndexed { index, size ->
                SegmentedButton(
                    selected = keySize == size,
                    onClick = {
                        keySize = size
                        prefs.keySize = size
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, KeySize.entries.size),
                ) { Text(stringResource(keySizeLabel(size))) }
            }
        }
        Text(
            stringResource(R.string.theme_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        var keySound by remember { mutableStateOf(prefs.keySound) }
        SettingSwitch(
            title = stringResource(R.string.key_sound),
            checked = keySound,
            onCheckedChange = { keySound = it; prefs.keySound = it },
        )

        var smart by remember { mutableStateOf(prefs.smartConjunct) }
        SettingSwitch(
            title = stringResource(R.string.smart_conjunct),
            subtitle = stringResource(R.string.smart_conjunct_desc),
            checked = smart,
            onCheckedChange = { smart = it; prefs.smartConjunct = it },
        )

        OutlinedTextField(
            value = tryText,
            onValueChange = onTryTextChange,
            label = { Text(stringResource(R.string.try_keyboard_here)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun keySizeLabel(size: KeySize) = when (size) {
    KeySize.SMALL -> R.string.size_small
    KeySize.MEDIUM -> R.string.size_medium
    KeySize.LARGE -> R.string.size_large
}

/** Find the host activity through any Context wrappers (Compose may wrap it). */
private fun Context.findActivity(): ComponentActivity? {
    var c: Context? = this
    while (c is android.content.ContextWrapper) {
        if (c is ComponentActivity) return c
        c = c.baseContext
    }
    return null
}
