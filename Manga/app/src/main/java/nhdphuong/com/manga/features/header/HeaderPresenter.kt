package nhdphuong.com.manga.features.header

import android.content.Context
import android.util.Log
import nhdphuong.com.manga.data.Tab
import nhdphuong.com.manga.data.Tag
import nhdphuong.com.manga.features.tags.TagsActivity
import javax.inject.Inject

/*
 * Created by nhdphuong on 4/10/18.
 */
class HeaderPresenter @Inject constructor(private val mView: HeaderContract.View,
                                          private val mContext: Context) : HeaderContract.Presenter {
    companion object {
        private const val TAG = "HeaderPresenter"
    }

    init {
        mView.setPresenter(this)
    }

    override fun start() {
        Log.d(TAG, "This is ${hashCode()}")
    }

    override fun goToTagList(@Tag tag: String) {
        TagsActivity.start(mContext, tag)
    }

    override fun stop() {

    }

}