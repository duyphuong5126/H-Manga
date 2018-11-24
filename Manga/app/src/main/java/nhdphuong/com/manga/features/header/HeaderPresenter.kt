package nhdphuong.com.manga.features.header

import android.content.Context
import nhdphuong.com.manga.DownloadManager
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.Tab
import javax.inject.Inject

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderPresenter @Inject constructor(private val mView: HeaderContract.View,
                                          private val mContext: Context) : HeaderContract.Presenter {
    companion object {
        private const val TAG = "HeaderPresenter"
    }

    private val mTagDownloadManager = DownloadManager.Companion.TagsDownloadManager

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        Logger.d(TAG, "This is ${hashCode()}")
    }

    override fun goToTagsList(tab: Tab) {
        if (mTagDownloadManager.isTagDownloading) {
            mView.showTagsDownloadingPopup()
        } else {
            mView.goToTagsList(tab)
        }
    }

    override fun stop() {

    }

}