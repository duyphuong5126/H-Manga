package nhdphuong.com.manga.features.header

import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.supports.INetworkUtils
import javax.inject.Inject

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderPresenter @Inject constructor(
    private val view: HeaderContract.View,
    private val networkUtils: INetworkUtils
) : HeaderContract.Presenter {
    companion object {
        private const val TAG = "HeaderPresenter"
    }

    private val isNetworkAvailable: Boolean get() = networkUtils.isNetworkConnected()

    private val tagDownloadManager = DownloadManager.Companion.TagsDownloadManager

    init {
        view.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "This is ${hashCode()}")
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
                doIfNetworkIsAvailable {
                    view.goToRecentList()
                }
            }
            Tab.FAVORITE -> {
                doIfNetworkIsAvailable {
                    view.goToFavoriteList()
                }
            }
            Tab.RANDOM -> {
                doIfNetworkIsAvailable {
                    view.goToRandomBook()
                }
            }
            else -> Unit
        }
    }

    private fun doIfNetworkIsAvailable(task: () -> Unit) {
        if (isNetworkAvailable) {
            task.invoke()
        } else {
            view.showNoNetworkPopup()
        }
    }

    override fun stop() {

    }
}
