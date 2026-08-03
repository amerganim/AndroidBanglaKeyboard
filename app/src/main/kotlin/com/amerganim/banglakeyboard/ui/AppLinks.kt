package com.amerganim.banglakeyboard.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast

/** Public support address — the same one published in the privacy policy. */
const val SUPPORT_EMAIL = "ganimtruthfinder@gmail.com"

/**
 * Open this app's Play Store listing so the user can rate it.
 *
 * Tries the Play Store app first and falls back to the web listing, since the
 * app ships on devices that may not have Play installed.
 */
fun openPlayStoreListing(context: Context) {
    val id = context.packageName
    val playApp = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val web = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$id"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(playApp)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(web)
        } catch (_: ActivityNotFoundException) {
            context.toast("Google Play is not available on this device.")
        }
    }
}

/**
 * Open the user's email app with a pre-filled support message.
 *
 * Nothing is sent by the app itself — this only hands a draft to an email client,
 * which keeps the keyboard's "no internet permission" guarantee intact. The app
 * version and device details are included in the visible body so the user can
 * read or delete them before sending.
 */
fun sendFeedbackEmail(context: Context, subject: String, bodyHeader: String) {
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "?"

    val body = buildString {
        append(bodyHeader)
        append("\n\n\n---\n")
        append("App: ${context.packageName} $version\n")
        append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        append("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
    }

    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, body)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.toast("No email app found. Write to $SUPPORT_EMAIL")
    }
}

private fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
