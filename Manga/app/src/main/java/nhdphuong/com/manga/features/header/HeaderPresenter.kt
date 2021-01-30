package nhdphuong.com.manga.features.header

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.supports.INetworkUtils
import nhdphuong.com.manga.usecase.GetFeedbackFormUseCase
import nhdphuong.com.manga.usecase.GetLatestSearchEntriesUseCase
import nhdphuong.com.manga.usecase.SaveSearchInfoUseCase
import javax.inject.Inject

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderPresenter @Inject constructor(
    private val getLatestSearchEntriesUseCase: GetLatestSearchEntriesUseCase,
    private val saveSearchInfoUseCase: SaveSearchInfoUseCase,
    private val getFeedbackFormUseCase: GetFeedbackFormUseCase,
    private val view: HeaderContract.View,
    private val networkUtils: INetworkUtils
) : HeaderContract.Presenter {
    companion object {
        private const val TAG = "HeaderPresenter"
        private const val MAXIMUM_SUGGESTION_ENTRIES = 1000
        private const val DEFAULT_FEEDBACK_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSc6QzFWTRnnpBKMMyryaaa8WL-w9rt1wkm1g7bAvMmFLYs2og/viewform?usp=sf_link"
    }

    private val compositeDisposable = CompositeDisposable()

    private val isNetworkAvailable: Boolean get() = networkUtils.isNetworkConnected()

    private val tagDownloadManager = DownloadManager.Companion.TagsDownloadManager

    private val searchEntries = ArrayList<String>()

    private var feedbackFormUrl: String = ""

    init {
        view.setPresenter(this)
    }

    override fun start() {
        view.setUpSuggestionList(searchEntries)
    }

    override fun goToTagsList(tab: Tab) {
        if (tagDownloadManager.isTagDownloading) {
            view.showTagsDownloadingPopup()
        } else {
            view.goToTagsList(tab)
        }
    }

    override fun processSelectedTab(tab: Tab) {
        when (tab) {
            Tab.RECENT -> {
                view.goToRecentList()
            }
            Tab.FAVORITE -> {
                view.goToFavoriteList()
            }
            Tab.RANDOM -> {
                doIfNetworkIsAvailable {
                    view.goToRandomBook()
                }
            }
            else -> Unit
        }
    }

    override fun saveSearchInfo(searchContent: String) {
        saveSearchInfoUseCase.execute(searchContent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d(TAG, "Saved entry $searchContent successfully")
                searchEntries.add(searchContent)
                view.updateSuggestionList()
            }, {
                Logger.e(TAG, "Failed to save search info $searchContent with error: $it")
            })
            .addTo(compositeDisposable)
    }

    override fun refreshTagData() {
        refreshTagList()
    }

    override fun requestFeedbackForm() {
        if (feedbackFormUrl.isNotBlank()) {
            view.navigateToFeedbackForm(feedbackFormUrl)
            return
        }

        getFeedbackFormUseCase.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d(TAG, "Feedback form $it")
                feedbackFormUrl = it
                view.navigateToFeedbackForm(feedbackFormUrl)
            }, {
                Logger.e(TAG, "Failed to get feedback form with error $it")
                view.navigateToFeedbackForm(DEFAULT_FEEDBACK_URL)
            }).addTo(compositeDisposable)
    }

    private fun refreshTagList() {
        getLatestSearchEntriesUseCase.execute(MAXIMUM_SUGGESTION_ENTRIES)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Logger.d(TAG, "${it.size} search entries were found")
                updateSearchList(it)
            }, {
                Logger.e(TAG, "Failed to get search entries with error: $it")
            })
            .addTo(compositeDisposable)
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    private fun doIfNetworkIsAvailable(task: () -> Unit) {
        if (isNetworkAvailable) {
            task.invoke()
        } else {
            view.showNoNetworkPopup()
        }
    }

    private fun updateSearchList(newSearchList: List<String>) {
        searchEntries.clear()
        searchEntries.addAll(newSearchList)
        view.updateSuggestionList()
    }
}
