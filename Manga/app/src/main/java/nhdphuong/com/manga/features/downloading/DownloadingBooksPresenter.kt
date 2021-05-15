package nhdphuong.com.manga.features.downloading

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.PendingItemUiModel
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel.TotalCountItem
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import nhdphuong.com.manga.usecase.GetPendingDownloadBookCountUseCase
import nhdphuong.com.manga.usecase.GetPendingDownloadBookListUseCase
import nhdphuong.com.manga.usecase.RemoveBookFromPendingDownloadListUseCase
import javax.inject.Inject

class DownloadingBooksPresenter @Inject constructor(
    private val view: DownloadingBooksContract.View,
    private val removeBookFromPendingDownloadListUseCase: RemoveBookFromPendingDownloadListUseCase,
    private val getPendingDownloadBookListUseCase: GetPendingDownloadBookListUseCase,
    private val getPendingDownloadBookCountUseCase: GetPendingDownloadBookCountUseCase,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : DownloadingBooksContract.Presenter {
    private val compositeDisposable = CompositeDisposable()
    private val pendingList = arrayListOf<PendingDownloadItemUiModel>()
    private val logger = Logger("DownloadingBooksPresenter")

    override fun start() {
        getPendingDownloadBookListUseCase.execute(PENDING_LIST_LIMIT)
            .doOnSuccess { pendingDownloadBooks ->
                pendingDownloadBooks.map {
                    PendingItemUiModel(it.bookId, it.bookTitle)
                }.let(pendingList::addAll)
            }.flatMap {
                getPendingDownloadBookCountUseCase.execute()
            }.doOnSuccess {
                if (it > 0) {
                    pendingList.add(TotalCountItem(it))
                }
            }
            .ignoreElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                view.showLoading()
            }.doAfterTerminate {
                view.hideLoading()
            }
            .subscribe({
                view.setUpPendingDownloadList(pendingList)
                if (pendingList.isEmpty()) {
                    view.showNothingView()
                } else {
                    view.hideNothingView()
                }
            }, {
                logger.e("Failed to get pending list with error $it")
                view.showNothingView()
            }).addTo(compositeDisposable)
    }

    override fun removePendingItem(bookId: String) {
        removeBookFromPendingDownloadListUseCase.execute(bookId)
            .doOnComplete {
                pendingList.removeAll { pendingItem ->
                    pendingItem is PendingItemUiModel && pendingItem.bookId == bookId
                }
            }.andThen(getPendingDownloadBookCountUseCase.execute())
            .doOnSuccess {
                pendingList.removeAll { pendingItem ->
                    pendingItem is TotalCountItem
                }
                if (it > 0) {
                    pendingList.add(TotalCountItem(it))
                }
            }
            .ignoreElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                logger.d("Remaining items ${pendingList.size}")
                view.updatePendingList(pendingList)
            }, {
                logger.e("Failed to remove book $bookId with error $it")
            }).addTo(compositeDisposable)
    }

    override fun updateDownloadingStarted(bookId: String) {
        io.launch {
            pendingList.indexOfFirst {
                it is PendingItemUiModel && it.bookId == bookId
            }.takeIf { it >= 0 }?.let { updatePosition ->
                main.launch {
                    view.updateDownloadingStarted(updatePosition)
                }
            }
        }
    }

    override fun updateDownloadingProgress(bookId: String, progress: Int, total: Int) {
        io.launch {
            pendingList.indexOfFirst {
                it is PendingItemUiModel && it.bookId == bookId
            }.takeIf { it >= 0 }?.let { updatePosition ->
                main.launch {
                    view.updateProgress(updatePosition, progress, total)
                }
            }
        }
    }

    override fun updateDownloadingCompleted(bookId: String) {
        io.launch {
            pendingList.indexOfFirst {
                it is PendingItemUiModel && it.bookId == bookId
            }.takeIf { it >= 0 }?.let { updatePosition ->
                main.launch {
                    view.updateCompletion(updatePosition)
                }
            }
        }
    }

    override fun updateDownloadFailure(bookId: String, failureCount: Int, total: Int) {
        io.launch {
            pendingList.indexOfFirst {
                it is PendingItemUiModel && it.bookId == bookId
            }.takeIf { it >= 0 }?.let { updatePosition ->
                main.launch {
                    view.updateFailure(updatePosition, failureCount, total)
                }
            }
        }
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    companion object {
        private const val PENDING_LIST_LIMIT = 1000
    }
}