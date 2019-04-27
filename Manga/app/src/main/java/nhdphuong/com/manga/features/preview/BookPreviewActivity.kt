package nhdphuong.com.manga.features.preview

import android.annotation.TargetApi
import android.app.Activity
import android.support.v4.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import javax.inject.Inject
import android.view.WindowManager


class BookPreviewActivity : AppCompatActivity() {
    @Suppress("unused")
    @Inject
    lateinit var mPresenter: BookPreviewPresenter

    companion object {
        fun start(fragment: Fragment, book: Book) {
            val intent = Intent(fragment.activity, BookPreviewActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            fragment.startActivityForResult(intent, Constants.BOOK_PREVIEW_RESULT)
        }

        private var mInstance: BookPreviewActivity? = null

        fun restart(book: Book) {
            mInstance?.let { bookPreviewActivity ->
                bookPreviewActivity.intent.putExtra(Constants.BOOK, book)
                bookPreviewActivity.recreate()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_preview)
        mInstance = this

        val book = intent.extras?.getSerializable(Constants.BOOK) as Book

        var bookPreviewFragment = supportFragmentManager.findFragmentById(R.id.clBookPreview)
                as BookPreviewFragment?
        if (bookPreviewFragment == null) {
            bookPreviewFragment = BookPreviewFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.clBookPreview, bookPreviewFragment)
                    .commitAllowingStateLoss()
        }

        NHentaiApp.instance.applicationComponent.plus(
                BookPreviewModule(bookPreviewFragment, book)
        ).inject(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.BLACK
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }
}
