package nhdphuong.com.manga.features.reader

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import javax.inject.Inject

class ReaderActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context, startReadingPage: Int, book: Book) {
            val intent = Intent(context, ReaderActivity::class.java)
            intent.putExtra(Constants.BOOK, book)
            intent.putExtra(Constants.START_PAGE, startReadingPage)
            context.startActivity(intent)
        }
    }

    @Suppress("unused")
    @Inject
    lateinit var mReadPresenter: ReaderPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        var readerFragment = supportFragmentManager.findFragmentById(R.id.clReaderFragment)
                as ReaderFragment?
        if (readerFragment == null) {
            readerFragment = ReaderFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.clReaderFragment, readerFragment)
                    .commitAllowingStateLoss()
        }

        val book = intent.getParcelableExtra(Constants.BOOK) as Book
        val startReadingPage = intent.getIntExtra(Constants.START_PAGE, 0)
        NHentaiApp.instance.applicationComponent.plus(
                ReaderModule(readerFragment, book, startReadingPage)
        ).inject(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        super.onResume()
        window?.statusBarColor = ContextCompat.getColor(this@ReaderActivity, R.color.grey_1)
    }
}
