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
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView

private const val TAG = "DialogHelper"
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
        Logger.d(TAG, "Current pos: $currentPos")
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
    val message = getString(R.string.downloading_tags_already_started_message)
    val cancel = getString(R.string.ok)
    val ok = getString(R.string.stop_downloading_tags)
    showOkDismissDialog(
        this,
        getString(R.string.downloading_tags_already_started_title),
        message,
        ok,
        cancel,
        onOk,
        onDismiss
    )
}

fun Activity.showUnSeenBookConfirmationDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val title = getString(R.string.un_seen_book_title)
    val message = getString(R.string.un_seen_book_description)
    val okString = getString(R.string.yes)
    val dismissString = getString(R.string.no)
    showOkDismissDialog(
        this,
        title,
        message,
        okString,
        dismissString,
        onOk,
        onDismiss
    )
}

fun Activity.showAdminEntryDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val title = getString(R.string.enter_admin_page_title)
    val message = getString(R.string.enter_admin_page_description)
    val okString = getString(R.string._continue)
    val dismissString = getString(R.string.exit)
    showOkDismissDialog(
        this,
        title,
        message,
        okString,
        dismissString,
        onOk,
        onDismiss
    )
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
    val okString = getString(R.string.view)
    val dismissString = getString(R.string.ok)
    showOkDismissDialog(
        this,
        title,
        message,
        okString,
        dismissString,
        onOk,
        onDismiss
    )
}

fun Activity.showDownloadingFinishedDialog(
    onOk: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val permissionTitle = getString(R.string.book_downloading_finished)
    val permissionDescription = getString(R.string.is_want_to_open_folder)
    val okString = getString(R.string.ok)
    val dismissString = getString(R.string.dismiss)
    showOkDismissDialog(
        this,
        permissionTitle,
        permissionDescription,
        okString,
        dismissString,
        onOk,
        onDismiss
    )
}

fun Activity.showStoragePermissionDialog(onOk: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    val permissionTitle = getString(R.string.permission_require)
    val permissionDescription = getString(R.string.storage_permission_require)
    val okString = getString(R.string.ok)
    val dismissString = getString(R.string.dismiss)
    showOkDismissDialog(
        this,
        permissionTitle,
        permissionDescription,
        okString,
        dismissString,
        onOk,
        onDismiss
    )
}

fun Activity.showGoToPageDialog(
    minimum: Int,
    maximum: Int,
    onOk: (number: Int) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val title = getString(R.string.jump_to_page)
    val errorMessage = getString(R.string.invalid_page)
    val okString = getString(R.string.ok)
    val dismissString = getString(R.string.dismiss)
    val inputHint = getString(R.string.page_number_hint) + " ($minimum - $maximum)"
    showOkDismissInputNumberDialog(
        this,
        title,
        errorMessage,
        minimum,
        maximum,
        okString,
        dismissString,
        inputHint,
        onOk,
        onDismiss
    )
}

@SuppressLint("InflateParams")
private fun showOkDismissDialog(
    activity: Activity,
    title: String,
    description: String,
    ok: String,
    dismiss: String,
    onOk: () -> Unit,
    onDismiss: () -> Unit
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
