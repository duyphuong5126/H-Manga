package nhdphuong.com.manga.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView

private const val DEFAULT_LOADING_INTERVAL = 700L

@SuppressLint("InflateParams", "SetTextI18n")
fun Activity.createLoadingDialog(loadingStringId: Int = R.string.loading): Dialog {
    val loadingString = getString(loadingStringId)
    val dotsArray = resources.getStringArray(R.array.dots)
    var currentPos = 0
    val dialog = Dialog(this)
    val layoutInflater = LayoutInflater.from(this)
    val contentView = layoutInflater.inflate(
        R.layout.layout_loading_dialog,
        null,
        false
    )
    val tvLoading: TextView = contentView.findViewById(R.id.tvLoading)
    val ivLoading: ImageView = contentView.findViewById(R.id.ivLoading)
    dialog.setContentView(contentView)
    dialog.setCancelable(false)
    ImageUtils.loadGifImage(R.raw.ic_loading_cat_transparent, ivLoading)
    val taskHandler = Handler(Looper.getMainLooper())
    val dotsUpdatingTask = Runnable {
        tvLoading.text = loadingString + dotsArray[currentPos]
        if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
    }
    val updateTask = object : Runnable {
        override fun run() {
            dotsUpdatingTask.run()
            taskHandler.postDelayed(this, DEFAULT_LOADING_INTERVAL)
        }
    }

    dialog.setOnShowListener {
        taskHandler.post(updateTask)
    }

    dialog.setOnDismissListener {
        taskHandler.removeCallbacksAndMessages(null)
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.window?.let { window ->
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }

    return dialog
}

fun Activity.showDoNotRecommendBookDialog(
    bookId: String,
    onOk: () -> Unit,
    onCancel: () -> Unit = {}
) {
    showOkDismissDialog(
        this,
        getString(R.string.do_not_recommend_book_title),
        getString(R.string.do_not_recommend_book_message, bookId),
        getString(R.string.yes),
        getString(R.string.no),
        onOk,
        onCancel,
        false
    )
}

fun Activity.showSuggestionRemovalConfirmationDialog(
    suggestion: String,
    onOk: () -> Unit,
    onCancel: () -> Unit = {}
) {
    showOkDismissDialog(
        this,
        getString(R.string.suggestion_removal_title),
        getString(R.string.suggestion_removal_message, suggestion),
        getString(R.string.yes),
        getString(R.string.no),
        onOk,
        onCancel,
        false
    )
}

fun Activity.showBookDownloadingFailureDialog(bookId: String, onOk: () -> Unit = {}) {
    showOkDialog(
        this,
        getString(R.string.downloading_failure),
        getString(R.string.downloading_book_failed_message, bookId),
        onOk
    )
}

fun Activity.showTagsDownloadingDialog(onOk: () -> Unit = {}) {
    val message = getString(R.string.is_downloading_tags)
    showOkDialog(
        this,
        getString(R.string.are_tags_being_downloaded),
        message,
        onOk
    )
}

fun Activity.showTagsNotAvailable(onOk: () -> Unit = {}) {
    showOkDialog(
        this,
        getString(R.string.under_construction_title),
        getString(R.string.feature_under_construction_description),
        onOk
    )
}

fun Activity.showThisBookDownloadingDialog(onOk: () -> Unit = {}) {
    val message = getString(R.string.is_downloading_this_book)
    showOkDialog(
        this,
        getString(R.string.is_book_being_downloaded),
        message,
        onOk
    )
}

fun Activity.showBookListRefreshingDialog(onOk: () -> Unit = {}) {
    showOkDialog(
        this,
        getString(R.string.book_list_refreshing_title),
        getString(R.string.book_list_refreshing_description),
        onOk
    )
}

fun Activity.showInternetRequiredDialog(onOk: () -> Unit = {}) {
    showOkDialog(
        this,
        getString(R.string.no_network_title),
        getString(R.string.no_network_description),
        onOk
    )
}

fun Activity.showTagDataBeingDownloadedDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val title = getString(R.string.downloading_tags_already_started_title)
    val message = getString(R.string.downloading_tags_already_started_message)
    val cancelButton = getString(R.string.ok)
    val okButton = getString(R.string.stop_downloading_tags)
    showOkDismissDialog(this, title, message, okButton, cancelButton, onOk, onDismiss)
}

fun Activity.showUnSeenBookConfirmationDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val title = getString(R.string.un_seen_book_title)
    val message = getString(R.string.un_seen_book_description)
    val okButton = getString(R.string.yes)
    val cancelButton = getString(R.string.no)
    showOkDismissDialog(this, title, message, okButton, cancelButton, onOk, onDismiss)
}

fun Activity.showAdminEntryDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val title = getString(R.string.enter_admin_page_title)
    val message = getString(R.string.enter_admin_page_description)
    val okString = getString(R.string._continue)
    val dismissString = getString(R.string.exit)
    showOkDismissDialog(this, title, message, okString, dismissString, onOk, onDismiss)
}

fun Activity.showBookDownloadingDialog(
    mediaId: String,
    onOk: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val title = getString(R.string.is_book_being_downloaded)
    val message = String.format(
        getString(R.string.is_downloading_another_book),
        mediaId
    )
    val okButton = getString(R.string.view)
    val cancelButton = getString(R.string.ok)
    showOkDismissDialog(this, title, message, okButton, cancelButton, onOk, onDismiss)
}

fun Activity.showDownloadingFinishedDialog(
    bookId: String,
    onOk: () -> Unit = {}
) {
    val title = getString(R.string.book_downloading_finished_title)
    val message = getString(R.string.book_downloading_finished_message, bookId)
    showOkDialog(this, title, message, onOk)
}

fun Activity.showBookDeletingConfirmationDialog(
    bookId: String,
    onOk: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val title = getString(R.string.book_deleting_confirmation_title)
    val message = getString(R.string.book_deleting_confirmation_message, bookId)
    val okButton = getString(R.string.yes)
    val cancelButton = getString(R.string.no)
    showOkDismissDialog(this, title, message, okButton, cancelButton, onOk, onCancel, true)
}

fun Activity.showGoToPageDialog(
    minimum: Int,
    maximum: Int,
    onOk: (number: Int) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val title = getString(R.string.jump_to_page)
    val errorMessage = getString(R.string.invalid_page)
    val okButton = getString(R.string.ok)
    val cancelButton = getString(R.string.cancel)
    val inputHint = getString(R.string.page_number_hint) + " ($minimum - $maximum)"
    showOkDismissInputNumberDialog(
        this,
        title,
        errorMessage,
        minimum,
        maximum,
        okButton,
        cancelButton,
        inputHint,
        onOk,
        onDismiss
    )
}

fun Activity.showInstallationConfirmDialog(
    versionCode: String,
    onOk: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val title = getString(R.string.installation_confirm_title)
    val message = getString(R.string.installation_confirm_message, versionCode)
    val okButton = getString(R.string.ok)
    val cancelButton = getString(R.string.cancel)
    showOkDismissDialog(this, title, message, okButton, cancelButton, onOk, onDismiss)
}

fun Activity.showFailedToUpgradeAppDialog(
    versionCode: String,
    onOk: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val title = getString(R.string.app_upgrade_failed_title)
    val message = getString(R.string.app_upgrade_failed_message, versionCode)
    val retry = getString(R.string.retry)
    val installManually = getString(R.string.install_manually)
    showOkDismissDialog(this, title, message, retry, installManually, onOk, onCancel, true)
}

fun Activity.showRestartAppDialog(
    onOk: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val title = getString(R.string.restart_app_title)
    val message = getString(R.string.restart_app_message)
    val ok = getString(R.string.restart_button)
    val cancel = getString(R.string.cancel)
    showOkDismissDialog(this, title, message, ok, cancel, onOk, onCancel, false)
}

fun Activity.showTryAlternativeDomainsDialog(
    onOk: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val title = getString(R.string.try_alter_native_domain_title)
    val message = getString(R.string.try_alter_native_domain_message)
    val ok = getString(R.string.try_it)
    val cancel = getString(R.string.cancel)
    showOkDismissDialog(this, title, message, ok, cancel, onOk, onCancel, false)
}

@SuppressLint("InflateParams")
private fun showOkDismissDialog(
    activity: Activity,
    title: String,
    description: String,
    ok: String,
    dismiss: String,
    onOk: () -> Unit,
    onDismiss: () -> Unit,
    canceledOnTouchOutside: Boolean = false
) {
    val contentView = activity.layoutInflater.inflate(
        R.layout.dialog_ok_dismiss,
        null,
        false
    )
    val dialog = Dialog(activity)
    val mtvTitle: MyTextView = contentView.findViewById(R.id.mtvDialogTitle)
    val mtvDescription: MyTextView = contentView.findViewById(R.id.mtvDialogDescription)
    val mbOk: MyButton = contentView.findViewById(R.id.mbOkButton)
    val mbDismiss: MyButton = contentView.findViewById(R.id.mbDismissButton)
    mbOk.text = ok
    mbDismiss.text = dismiss
    mtvTitle.text = title
    mtvDescription.text = description
    contentView.findViewById<MyButton>(R.id.mbOkButton).setOnClickListener {
        dialog.dismiss()
        onOk()
    }
    contentView.findViewById<MyButton>(R.id.mbDismissButton).setOnClickListener {
        dialog.dismiss()
        onDismiss()
    }
    dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
    dialog.setContentView(contentView)
    dialog.show()
    dialog.window?.let { window ->
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }
}

@SuppressLint("InflateParams")
private fun showOkDismissInputNumberDialog(
    activity: Activity,
    title: String,
    errorMessage: String,
    minimum: Int,
    maximum: Int,
    ok: String,
    dismiss: String,
    inputHint: String,
    onOk: (number: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val contentView = activity.layoutInflater.inflate(
        R.layout.dialog_ok_dismiss_input_number,
        null,
        false
    )
    val dialog = Dialog(activity)
    val mtvTitle: MyTextView = contentView.findViewById(R.id.mtvDialogTitle)
    val mtvError: MyTextView = contentView.findViewById(R.id.mtvError)
    val edtInputNumber: EditText = contentView.findViewById(R.id.edtInputNumber)
    val mbOk: MyButton = contentView.findViewById(R.id.mbOkButton)
    val mbDismiss: MyButton = contentView.findViewById(R.id.mbDismissButton)
    mbOk.text = ok
    mbDismiss.text = dismiss
    mtvTitle.text = title
    mtvError.text = errorMessage
    edtInputNumber.hint = inputHint
    contentView.findViewById<MyButton>(R.id.mbOkButton).setOnClickListener {
        try {
            val number = edtInputNumber.text.toString().toInt()
            if (number in minimum..maximum) {
                mtvError.gone()
                dialog.dismiss()
                onOk.invoke(number)
            } else {
                mtvError.becomeVisible()
            }
        } catch (error: Throwable) {
            mtvError.becomeVisible()
        }
    }
    contentView.findViewById<MyButton>(R.id.mbDismissButton).setOnClickListener {
        dialog.dismiss()
        onDismiss()
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.setContentView(contentView)
    dialog.show()
    dialog.window?.let { window ->
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }
}

@SuppressLint("InflateParams")
private fun showOkDialog(
    activity: Activity,
    title: String,
    description: String,
    onOk: () -> Unit
) {
    val contentView = activity.layoutInflater.inflate(
        R.layout.dialog_ok,
        null,
        false
    )
    val dialog = Dialog(activity)
    val mtvPermissionTitle: MyTextView = contentView.findViewById(R.id.mtvDialogTitle)
    mtvPermissionTitle.text = title
    val mtvDescription: MyTextView = contentView.findViewById(R.id.mtvDialogDescription)
    mtvDescription.text = description
    contentView.findViewById<MyButton>(R.id.mbOkButton).setOnClickListener {
        dialog.dismiss()
        onOk()
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.setContentView(contentView)
    dialog.show()
    dialog.window?.let { window ->
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        window.decorView.setBackgroundResource(android.R.color.transparent)
    }
}
