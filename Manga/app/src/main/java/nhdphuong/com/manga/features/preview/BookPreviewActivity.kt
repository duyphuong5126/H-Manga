package nhdphuong.com.manga.features.preview

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import javax.inject.Inject
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicBoolean
import nhdphuong.com.manga.Constants.Companion.RECENT_DATA_UPDATED_ACTION
import nhdphuong.com.manga.Constants.Companion.REFRESH_DOWNLOADED_BOOK_LIST
import nhdphuong.com.manga.Constants.Companion.TAG_SELECTED_ACTION


class BookPreviewActivity : AppCompatActivity() {
    @Suppress("unused")
    @Inject
    lateinit var presenter: BookPreviewPresenter

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_preview)
        started.compareAndSet(false, true)
        instance = this

        intent.extras?.getParcelable<Book>(Constants.BOOK)?.let { book ->
            var bookPreviewFragment: BookPreviewFragment? =
                supportFragmentManager.findFragmentById(R.id.clBookPreview)
                        as BookPreviewFragment?
            if (bookPreviewFragment == null) {
                bookPreviewFragment = BookPreviewFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.clBookPreview, bookPreviewFragment)
                    .commitAllowingStateLoss()
            }
            intent.extras?.getBoolean(Constants.VIEW_DOWNLOADED_DATA, false)
                ?.let { viewDownloadedData ->
                    bookPreviewFragment.arguments = Bundle().apply {
                        putBoolean(Constants.VIEW_DOWNLOADED_DATA, viewDownloadedData)
                    }
                }

            NHentaiApp.instance.applicationComponent.plus(
                BookPreviewModule(bookPreviewFragment, book)
            ).inject(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        started.compareAndSet(true, false)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.BLACK
    }

    override fun finish() {
        val action = intent.action
        if (action != TAG_SELECTED_ACTION && action != REFRESH_DOWNLOADED_BOOK_LIST) {
            intent.action = RECENT_DATA_UPDATED_ACTION
            setResult(Activity.RESULT_OK, intent)
        }
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    companion object {
        private val started = AtomicBoolean(false)
        val isStarted: Boolean get() = started.get()

        fun start(fragment: Fragment, launcher: ActivityResultLauncher<Intent>, book: Book) {
            val intent = Intent(fragment.activity, BookPreviewActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            launcher.launch(intent)
            fragment.activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        fun startViewDownloadedData(
            activity: AppCompatActivity,
            launcher: ActivityResultLauncher<Intent>,
            book: Book
        ) {
            val intent = Intent(activity, BookPreviewActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            intent.putExtra(Constants.VIEW_DOWNLOADED_DATA, true)
            launcher.launch(intent)
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        private var instance: BookPreviewActivity? = null

        fun restart(book: Book, viewDownLoadedData: Boolean = false) {
            instance?.let { bookPreviewActivity ->
                bookPreviewActivity.intent.apply {
                    putExtra(Constants.BOOK, book)
                    putExtra(Constants.VIEW_DOWNLOADED_DATA, viewDownLoadedData)
                }
                bookPreviewActivity.recreate()
                bookPreviewActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }
}
