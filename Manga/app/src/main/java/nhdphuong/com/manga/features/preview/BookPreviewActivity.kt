package nhdphuong.com.manga.features.preview

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class BookPreviewActivity : AppCompatActivity() {
    @Suppress("unused")
    @Inject
    lateinit var presenter: BookPreviewPresenter

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_preview)
        instance = this

        intent.extras?.getParcelable<Book>(Constants.BOOK)?.let { book ->
            var bookPreviewFragment: BookPreviewFragment? =
                supportFragmentManager.findFragmentById(R.id.clBookPreview)
                        as BookPreviewFragment?
            if (bookPreviewFragment == null) {
                bookPreviewFragment = BookPreviewFragment()
                intent.extras?.getBoolean(Constants.VIEW_DOWNLOADED_DATA, false)
                    ?.let { viewDownloadedData ->
                        bookPreviewFragment.arguments = Bundle().apply {
                            putBoolean(Constants.VIEW_DOWNLOADED_DATA, viewDownloadedData)
                        }
                    }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.clBookPreview, bookPreviewFragment)
                    .commitAllowingStateLoss()
            }

            NHentaiApp.instance.applicationComponent.plus(
                BookPreviewModule(bookPreviewFragment, book)
            ).inject(this)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.BLACK
    }

    override fun finish() {
        if (intent.action != Constants.TAG_SELECTED_ACTION) {
            intent.action = Constants.RECENT_DATA_UPDATED_ACTION
        }
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    companion object {
        fun start(fragment: Fragment, book: Book) {
            val intent = Intent(fragment.activity, BookPreviewActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            fragment.startActivityForResult(intent, Constants.BOOK_PREVIEW_RESULT)
        }

        fun startViewDownloadedData(context: Context, book: Book) {
            val intent = Intent(context, BookPreviewActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            intent.putExtra(Constants.VIEW_DOWNLOADED_DATA, true)
            context.startActivity(intent)
        }

        private var instance: BookPreviewActivity? = null

        fun restart(book: Book) {
            instance?.let { bookPreviewActivity ->
                bookPreviewActivity.intent.putExtra(Constants.BOOK, book)
                bookPreviewActivity.recreate()
            }
        }
    }
}
