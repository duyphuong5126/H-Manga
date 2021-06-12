package nhdphuong.com.manga.features.reader

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nhdphuong.com.manga.Constants.Companion.BOOK
import nhdphuong.com.manga.Constants.Companion.START_PAGE
import nhdphuong.com.manga.Constants.Companion.VIEW_DOWNLOADED_DATA
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import javax.inject.Inject

class ReaderActivity : AppCompatActivity() {
    companion object {
        fun start(
            fragment: Fragment,
            launcher: ActivityResultLauncher<Intent>,
            startReadingPage: Int,
            book: Book,
            viewDownloadedData: Boolean
        ) {
            fragment.context?.let { context ->
                val intent = Intent(context, ReaderActivity::class.java)
                intent.putExtra(BOOK, book)
                intent.putExtra(START_PAGE, startReadingPage)
                intent.putExtra(VIEW_DOWNLOADED_DATA, viewDownloadedData)
                launcher.launch(intent)
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
            intent.extras?.getBoolean(VIEW_DOWNLOADED_DATA, false)
                ?.let { viewDownloadedData ->
                    readerFragment.arguments = Bundle().apply {
                        putBoolean(VIEW_DOWNLOADED_DATA, viewDownloadedData)
                    }
                }
            supportFragmentManager.beginTransaction()
                .replace(R.id.clReaderFragment, readerFragment)
                .commitAllowingStateLoss()
        }

        val book = intent.getParcelableExtra(BOOK) as Book?
        val startReadingPage = intent.getIntExtra(START_PAGE, 0)
        val readerComponent = NHentaiApp.instance.applicationComponent.plus(
            ReaderModule(readerFragment, book!!, startReadingPage)
        )
        readerComponent.inject(this)
        readerComponent.inject(readerFragment)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
