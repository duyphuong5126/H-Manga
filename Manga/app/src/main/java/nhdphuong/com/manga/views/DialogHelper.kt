package nhdphuong.com.manga.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.supports.ImageUtils
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView

class DialogHelper {
    companion object {
        private const val TAG = "DialogHelper"

        @SuppressLint("InflateParams", "SetTextI18n")
        fun showLoadingDialog(activity: Activity): Dialog {
            val dotsArray = activity.resources.getStringArray(R.array.dots)
            var currentPos = 0
            val loadingString = activity.getString(R.string.loading)
            val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar)
            val layoutInflater = activity.layoutInflater
            val contentView = layoutInflater.inflate(R.layout.layout_loading_dialog, null, false)
            val tvLoading: TextView = contentView.findViewById(R.id.tvLoading)
            val ivLoading: ImageView = contentView.findViewById(R.id.ivLoading)
            dialog.setContentView(contentView)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            ImageUtils.loadGifImage(R.raw.ic_loading_cat_transparent, ivLoading)
            val onFinishTask = runScheduledTaskOnMainThread({
                Logger.d(TAG, "Current pos: $currentPos")
                tvLoading.text = loadingString + dotsArray[currentPos]
                if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
            }, 700)

            dialog.setOnDismissListener {
                onFinishTask()
            }

            return dialog
        }

        fun showStoragePermissionDialog(activity: Activity, onOk: () -> Unit, onDismiss: () -> Unit) {
            val permissionTitle = activity.getString(R.string.permission_require)
            val permissionDescription = activity.getString(R.string.storage_permission_require)
            val okString = activity.getString(R.string.ok)
            val dismissString = activity.getString(R.string.dismiss)
            showOkDismissDialog(activity, permissionTitle, permissionDescription, okString, dismissString, onOk, onDismiss)
        }

        fun showBookDownloadingDialog(activity: Activity, mediaId: String, onOk: () -> Unit, onDismiss: () -> Unit) {
            val title = activity.getString(R.string.is_book_being_downloaded)
            val message = String.format(activity.getString(R.string.is_downloading_another_book), mediaId)
            val okString = activity.getString(R.string.view)
            val dismissString = activity.getString(R.string.ok)
            showOkDismissDialog(activity, title, message, okString, dismissString, onOk, onDismiss)
        }

        fun showAdminEntryDialog(activity: Activity, onOk: () -> Unit, onDismiss: () -> Unit) {
            val title = activity.getString(R.string.enter_admin_page_title)
            val message = activity.getString(R.string.enter_admin_page_description)
            val okString = activity.getString(R.string._continue)
            val dismissString = activity.getString(R.string.exit)
            showOkDismissDialog(activity, title, message, okString, dismissString, onOk, onDismiss)
        }

        fun showThisBookDownloadingDialog(activity: Activity, onOk: () -> Unit) {
            val message = activity.getString(R.string.is_downloading_this_book)
            showOkDialog(activity, activity.getString(R.string.is_book_being_downloaded), message, onOk)
        }

        fun showBookListRefreshingDialog(activity: Activity, onOk: () -> Unit) {
            showOkDialog(activity, activity.getString(R.string.book_list_refreshing_title),
                    activity.getString(R.string.book_list_refreshing_description), onOk)
        }

        fun showTagsNotAvailable(activity: Activity, onOk: () -> Unit) {
            showOkDialog(activity, activity.getString(R.string.under_construction_title),
                    activity.getString(R.string.feature_under_construction_description), onOk)
        }

        fun showTagsDownloadingDialog(activity: Activity, onOk: () -> Unit) {
            val message = activity.getString(R.string.is_downloading_tags)
            showOkDialog(activity, activity.getString(R.string.are_tags_being_downloaded), message, onOk)
        }

        @SuppressLint("InflateParams")
        private fun showOkDialog(activity: Activity, title: String, description: String, onOk: () -> Unit) {
            val contentView = activity.layoutInflater.inflate(R.layout.dialog_ok, null, false)
            val dialog = Dialog(activity)
            val mtvPermissionTitle: MyTextView = contentView.findViewById(R.id.mtvDialogTitle)
            mtvPermissionTitle.text = title
            val mtvDescription: MyTextView = contentView.findViewById(R.id.mtvDialogDescription)
            mtvDescription.text = description
            contentView.findViewById<MyButton>(R.id.mbOkButton).setOnClickListener {
                dialog.dismiss()
                onOk()
            }
            dialog.setContentView(contentView)
            dialog.show()
            dialog.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setGravity(Gravity.CENTER)
                window.decorView.setBackgroundResource(android.R.color.transparent)
            }
        }

        @SuppressLint("InflateParams")
        private fun showOkDismissDialog(activity: Activity, title: String, description: String, ok: String, dismiss: String,
                                        onOk: () -> Unit, onDismiss: () -> Unit) {
            val contentView = activity.layoutInflater.inflate(R.layout.dialog_ok_dismiss, null, false)
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
            dialog.setContentView(contentView)
            dialog.show()
            dialog.window?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setGravity(Gravity.CENTER)
                window.decorView.setBackgroundResource(android.R.color.transparent)
            }
        }

        private fun runScheduledTaskOnMainThread(task: () -> Unit, timeInterval: Long): () -> Unit {
            val handler = Handler(Looper.getMainLooper())
            var canFinish = false
            val updateTask = object : Runnable {
                override fun run() {
                    if (!canFinish) {
                        task()
                        handler.postDelayed(this, timeInterval)
                    }
                }
            }
            handler.post {
                updateTask.run()
            }
            return {
                Logger.d(TAG, "Task cancelled")
                canFinish = true
                handler.removeCallbacksAndMessages(null)
            }
        }
    }
}