package nhdphuong.com.manga.supports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nhdphuong.com.manga.views.customs.MyTextView

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Context.openEmailApp(
    emailAddress: String,
    appVersion: String,
    subject: String = "[nHentai Android - v$appVersion] Feedback/ Suggestion",
    body: String = ""
) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "plain/text"
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    startActivity(Intent.createChooser(intent, "Choose an email app"))
}

fun Context.copyToClipBoard(label: String, text: String) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
        val clip: ClipData = ClipData.newPlainText(label, text)
        it.setPrimaryClip(clip)
    }
}

fun MyTextView.addClickAbleText(url: String, alias: String, onClick: (url: String) -> Unit) {
    this.text = url.toClickableLink(alias, onClick)
    this.movementMethod = LinkMovementMethod()
}

fun String.toClickableLink(alias: String, onClick: (url: String) -> Unit): SpannableString {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick.invoke(this@toClickableLink)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
    val spannableString = SpannableString(alias)
    spannableString.setSpan(clickableSpan, 0, alias.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannableString
}

fun CoroutineScope.doInIOContext(task: () -> Unit) {
    launch {
        withContext(Dispatchers.IO) {
            task.invoke()
        }
    }
}
