package nhdphuong.com.manga.features.reader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import javax.inject.Inject

class ReaderActivity : AppCompatActivity() {
    companion object {
        fun start(
            fragment: Fragment,
            startReadingPage: Int,
            book: Book,
            viewDownloadedData: Boolean
        ) {
            fragment.context?.let { context ->
                val intent = Intent(context, ReaderActivity::class.java)
                intent.putExtra(Constants.BOOK, book)
                intent.putExtra(Constants.START_PAGE, startReadingPage)
                intent.putExtra(Constants.VIEW_DOWNLOADED_DATA, viewDownloadedData)
                fragment.startActivityForResult(intent, Constants.READING_REQUEST)
            }
        }
    }

    @Suppress("unused")
    @Inject
    lateinit var readPresenter: ReaderPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        var readerFragment = supportFragmentManager.findFragmentById(R.id.clReaderFragment)
                as ReaderFragment?
        if (readerFragment == null) {
            readerFragment = ReaderFragment()
            intent.extras?.getBoolean(Constants.VIEW_DOWNLOADED_DATA, false)
                ?.let { viewDownloadedData ->
                    readerFragment.arguments = Bundle().apply {
                        putBoolean(Constants.VIEW_DOWNLOADED_DATA, viewDownloadedData)
                    }
                }
            supportFragmentManager.beginTransaction()
                .replace(R.id.clReaderFragment, readerFragment)
                .commitAllowingStateLoss()
        }

        val book = intent.getParcelableExtra(Constants.BOOK) as Book
        val startReadingPage = intent.getIntExtra(Constants.START_PAGE, 0)
        val readerComponent = NHentaiApp.instance.applicationComponent.plus(
            ReaderModule(readerFragment, book, startReadingPage)
        )
        readerComponent.inject(this)
        readerComponent.inject(readerFragment)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
