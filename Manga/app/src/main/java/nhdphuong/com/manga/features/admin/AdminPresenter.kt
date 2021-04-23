package nhdphuong.com.manga.features.admin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.SharedPreferencesManager
import nhdphuong.com.manga.data.entity.RemoteBookResponse
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class AdminPresenter @Inject constructor(
    private val view: AdminContract.View,
    private val bookRepository: BookRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : AdminContract.Presenter {
    companion object {
        private const val TAG = "AdminPresenter"
    }

    init {
        view.setPresenter(this)
    }

    private val numberOfPage = AtomicLong(0)

    override fun start() {
        io.launch {
            bookRepository.getBookByPage(1, SortOption.Recent).let {
                if (it is RemoteBookResponse.Success) {
                    numberOfPage.compareAndSet(0, it.remoteBook.numOfPages)
                    main.launch {
                        if (view.isActive()) {
                            view.showNumberOfPages(numberOfPage.get())
                        }
                    }
                }
            }
            Logger.d(TAG, "Number Of pages=${numberOfPage.get()}")
        }
    }

    override fun stop() {

    }

    override fun startDownloading() {
        if (numberOfPage.get() > 0) {
            view.startDownloadingTagData(numberOfPage.get())
        }
    }

    override fun toggleCensored(censored: Boolean) {
        sharedPreferencesManager.isCensored = censored
        view.restartApp()
    }
}
